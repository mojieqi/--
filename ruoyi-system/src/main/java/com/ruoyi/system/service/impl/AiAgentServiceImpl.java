package com.ruoyi.system.service.impl;

import com.ruoyi.system.agent.*;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.IAiAgentService;
import com.ruoyi.system.service.IEmbeddingService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent核心Service实现
 *
 * 编排Agent引擎、会话管理、知识库检索、消息持久化
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiAgentServiceImpl implements IAiAgentService {

    @Autowired
    private AiConversationMapper conversationMapper;

    @Autowired
    private AiConversationMessageMapper messageMapper;

    @Autowired
    private AiPromptTemplateMapper promptTemplateMapper;

    @Autowired
    private AiLlmConfigMapper llmConfigMapper;

    @Autowired
    private AiMessageKbRefMapper kbRefMapper;

    @Autowired
    private IEmbeddingService embeddingService;

    @Autowired
    private RestTemplate restTemplate;

    /** SseEmitter 超时时间(毫秒) */
    private static final long SSE_TIMEOUT = 300000L; // 5分钟

    /** RAG上下文最大字符数 */
    private static final int RAG_MAX_CHARS = 2000;

    @Override
    public SseEmitter chat(Long conversationId, String userMessage) {
        // 1. 超时SSE
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 2. 加载会话
        AiConversation conversation = conversationMapper.selectAiConversationById(conversationId);
        if (conversation == null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("会话不存在"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 3. 加载LLM配置
        AiLlmConfig llmConfig = llmConfigMapper.selectAiLlmConfigById(conversation.getLlmConfigId());
        if (llmConfig == null || StringUtils.isEmpty(llmConfig.getApiKey())) {
            try {
                emitter.send(SseEmitter.event().name("error").data("LLM配置无效，API Key未设置"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 4. 加载系统提示词
        String systemPrompt = loadSystemPrompt(conversation);

        // 5. 加载历史消息
        List<AiConversationMessage> history = messageMapper.selectAiConversationMessageList(
                new AiConversationMessage() {{ setConversationId(conversationId); }}
        );

        // 6. 知识库检索 (RAG)
        String knowledgeContext = searchKnowledgeBase(conversation, userMessage);

        // 7. 构建Agent上下文
        AgentContext context = AgentContext.builder()
                .conversationId(conversationId)
                .systemPrompt(systemPrompt)
                .history(history)
                .knowledgeContext(knowledgeContext)
                .userMessage(userMessage)
                .llmConfigId(llmConfig.getConfigId())
                .modelName(llmConfig.getDefaultModel())
                .baseUrl(llmConfig.getBaseUrl())
                .apiKey(llmConfig.getApiKey())
                .maxTokens(conversation.getMaxTokens())
                .temperature(0.7)
                .build();

        // 8. 创建Agent引擎并执行
        AgentEngine engine = createEngine();
        engine.execute(context, emitter, new AgentEngine.AgentCallback() {
            @Override
            public void onUserMessage(AiConversationMessage userMsg) {
                messageMapper.insertAiConversationMessage(userMsg);
            }

            @Override
            public void onAssistantMessage(AiConversationMessage assistantMsg, String summary) {
                messageMapper.insertAiConversationMessage(assistantMsg);
                conversationMapper.updateMessageCount(conversationId);

                // 更新会话标题(首次对话时用用户消息的前30字)
                if (history.isEmpty() && conversation.getTitle().equals("新对话")) {
                    AiConversation update = new AiConversation();
                    update.setConversationId(conversationId);
                    String title = userMessage.length() > 30 ? userMessage.substring(0, 30) + "..." : userMessage;
                    update.setTitle(title);
                    conversationMapper.updateAiConversation(update);
                }
            }
        });

        // 9. 处理SSE异常和超时
        emitter.onTimeout(() -> {});
        emitter.onError(ex -> {});
        emitter.onCompletion(() -> {});

        return emitter;
    }

    @Override
    public List<AiConversationMessage> getMessages(Long conversationId) {
        AiConversationMessage query = new AiConversationMessage();
        query.setConversationId(conversationId);
        return messageMapper.selectAiConversationMessageList(query);
    }

    @Override
    public int saveMessage(AiConversationMessage message) {
        return messageMapper.insertAiConversationMessage(message);
    }

    // --- private helpers ---

    private AgentEngine createEngine() {
        return new AgentEngine(
                new MemoryManager(),
                new PromptBuilder(),
                new LlmCaller(restTemplate)
        );
    }

    private String loadSystemPrompt(AiConversation conversation) {
        if (conversation.getPromptTemplateId() != null) {
            AiPromptTemplate template = promptTemplateMapper.selectAiPromptTemplateById(
                    conversation.getPromptTemplateId());
            if (template != null && "0".equals(template.getStatus()) &&
                    StringUtils.isNotEmpty(template.getPromptContent())) {
                return template.getPromptContent();
            }
        }
        return null;
    }

    private String searchKnowledgeBase(AiConversation conversation, String userMessage) {
        if (conversation.getKbId() == null) return null;

        try {
            List<IEmbeddingService.ChunkSearchResult> results =
                    embeddingService.searchInBase(conversation.getKbId(), userMessage, 5);
            if (results.isEmpty()) return null;
            return embeddingService.buildKnowledgeContext(results, RAG_MAX_CHARS);
        } catch (Exception e) {
            return null;
        }
    }
}
