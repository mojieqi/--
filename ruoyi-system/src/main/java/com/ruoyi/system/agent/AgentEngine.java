package com.ruoyi.system.agent;

import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.system.domain.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * Agent引擎 — 编排Agent核心流程
 *
 * 流程:
 * 1. 加载会话上下文(系统提示词+历史+知识库)
 * 2. 记忆管理(窗口检查+摘要压缩)
 * 3. 构建提示词消息数组
 * 4. 调用LLM流式生成
 * 5. 保存消息+更新会话
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AgentEngine {

    private final MemoryManager memoryManager;
    private final PromptBuilder promptBuilder;
    private final LlmCaller llmCaller;

    public AgentEngine(MemoryManager memoryManager, PromptBuilder promptBuilder, LlmCaller llmCaller) {
        this.memoryManager = memoryManager;
        this.promptBuilder = promptBuilder;
        this.llmCaller = llmCaller;
    }

    /**
     * 执行对话
     *
     * @param context       Agent上下文
     * @param emitter       SSE发射器
     * @param callback      完成回调(assistant消息 + 需要保存的用户消息)
     */
    public void execute(AgentContext context, SseEmitter emitter, AgentCallback callback) {
        try {
            // 1. 记忆管理: 获取窗口消息 + 生成摘要
            List<AiConversationMessage> allHistory = context.getHistory();
            String summary = null;

            if (allHistory != null && memoryManager.needCompression(allHistory)) {
                // 获取早期消息文本用于生成摘要
                String earlyText = memoryManager.getEarlyMessagesForSummary(allHistory);
                if (earlyText != null) {
                    // 使用窗口内的消息作为实际历史
                    JSONArray summaryReq = promptBuilder.buildSummaryRequest(earlyText);
                    try {
                        summary = llmCaller.generateSummary(context, summaryReq);
                    } catch (Exception e) {
                        // 摘要生成失败不阻塞主流程
                    }
                }
            }

            // 2. 构建LLM消息数组
            JSONArray messages = promptBuilder.buildMessages(context, summary);

            // 3. 先保存用户消息
            AiConversationMessage userMsg = new AiConversationMessage();
            userMsg.setConversationId(context.getConversationId());
            userMsg.setRole("user");
            userMsg.setContent(context.getUserMessage());
            userMsg.setTokenCount(memoryManager.estimateTokens(context.getUserMessage()));
            userMsg.setCreateTime(new Date());
            callback.onUserMessage(userMsg);

            // 4. 流式调用LLM
            final String finalSummary = summary;
            llmCaller.chatStream(context, messages, emitter, fullContent -> {
                // 5. 保存助手回复
                AiConversationMessage assistantMsg = new AiConversationMessage();
                assistantMsg.setConversationId(context.getConversationId());
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(fullContent);
                assistantMsg.setTokenCount(memoryManager.estimateTokens(fullContent));
                assistantMsg.setCreateTime(new Date());
                callback.onAssistantMessage(assistantMsg, finalSummary);
            });
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("Agent执行异常: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    /**
     * 获取MemoryManager (供外部使用)
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    /**
     * Agent回调接口
     */
    public interface AgentCallback {
        /** 用户消息已创建 */
        void onUserMessage(AiConversationMessage userMsg);

        /** 助手消息已生成(含完整内容) */
        void onAssistantMessage(AiConversationMessage assistantMsg, String summary);
    }
}
