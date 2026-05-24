package com.ruoyi.system.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.agent.LlmCaller.ToolCall;
import com.ruoyi.system.agent.tool.ToolRegistry;
import com.ruoyi.system.domain.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

/**
 * Agent引擎 — 编排Agent核心流程，支持 Function Calling 工具调用循环
 *
 * 流程:
 * 1. 加载会话上下文(系统提示词+历史+知识库)
 * 2. 记忆管理(窗口检查+摘要压缩)
 * 3. 构建提示词消息数组
 * 4. 工具调用循环 (Phase 4.3):
 *    → 调用LLM流式生成(含tools)
 *    → 如果LLM返回 tool_calls: 执行工具 → 注入结果 → 再次调用LLM
 *    → 如果LLM返回 content: 流式输出并结束
 * 5. 保存消息+更新会话
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AgentEngine {

    /** 工具调用循环最大迭代次数(防止死循环) */
    private static final int MAX_TOOL_LOOP = 5;

    private final MemoryManager memoryManager;
    private final PromptBuilder promptBuilder;
    private final LlmCaller llmCaller;
    private final ToolRegistry toolRegistry;

    public AgentEngine(MemoryManager memoryManager, PromptBuilder promptBuilder,
                       LlmCaller llmCaller, ToolRegistry toolRegistry) {
        this.memoryManager = memoryManager;
        this.promptBuilder = promptBuilder;
        this.llmCaller = llmCaller;
        this.toolRegistry = toolRegistry;
    }

    /**
     * 执行对话 (含工具调用循环)
     *
     * @param context   Agent上下文
     * @param emitter   SSE发射器
     * @param callback  完成回调
     */
    public void execute(AgentContext context, SseEmitter emitter, AgentCallback callback) {
        try {
            // ===== 1. 记忆管理 =====
            List<AiConversationMessage> allHistory = context.getHistory();
            String summary = null;

            if (allHistory != null && memoryManager.needCompression(allHistory)) {
                String earlyText = memoryManager.getEarlyMessagesForSummary(allHistory);
                if (earlyText != null) {
                    JSONArray summaryReq = promptBuilder.buildSummaryRequest(earlyText);
                    try {
                        summary = llmCaller.generateSummary(context, summaryReq);
                    } catch (Exception e) {
                        // 摘要生成失败不阻塞主流程
                    }
                }
            }

            // ===== 2. 构建消息数组 =====
            JSONArray messages = promptBuilder.buildMessages(context, summary);

            // ===== 3. 保存用户消息 =====
            AiConversationMessage userMsg = new AiConversationMessage();
            userMsg.setConversationId(context.getConversationId());
            userMsg.setRole("user");
            userMsg.setContent(context.getUserMessage());
            userMsg.setTokenCount(memoryManager.estimateTokens(context.getUserMessage()));
            userMsg.setCreateTime(new Date());
            callback.onUserMessage(userMsg);

            // ===== 4. 构建 tools 参数 =====
            JSONArray tools = (toolRegistry != null && toolRegistry.hasAnyTools())
                    ? toolRegistry.buildToolsJson() : null;

            // ===== 5. 工具调用循环 =====
            final String finalSummary = summary;
            doToolCallingLoop(context, messages, tools, emitter, callback, finalSummary, 0);

        } catch (Exception e) {
            sendError(emitter, "Agent执行异常: " + e.getMessage());
        }
    }

    // ======================== 工具调用循环 ========================

    /**
     * 递归式工具调用循环
     *
     * @param context    Agent上下文
     * @param messages   当前消息数组(每次迭代会追加 assistant(tool_calls) + tool results)
     * @param tools      tools定义(不变)
     * @param emitter    SSE
     * @param callback   回调
     * @param summary    对话摘要
     * @param loopCount  当前迭代次数
     */
    private void doToolCallingLoop(AgentContext context, JSONArray messages, JSONArray tools,
                                   SseEmitter emitter, AgentCallback callback,
                                   String summary, int loopCount) {
        if (loopCount >= MAX_TOOL_LOOP) {
            sendError(emitter, "工具调用次数超过上限(" + MAX_TOOL_LOOP + "),请简化问题重试");
            return;
        }

        llmCaller.chatStreamWithTools(context, messages, tools, emitter, new LlmCaller.StreamCallback() {

            private final StringBuilder contentBuf = new StringBuilder();
            private boolean isToolCall = false;

            @Override
            public void onContent(String deltaContent) {
                contentBuf.append(deltaContent);
                try {
                    emitter.send(SseEmitter.event().name("message").data(deltaContent));
                } catch (IOException ignored) {}
            }

            @Override
            public void onToolCallStart() {
                isToolCall = true;
                try {
                    emitter.send(SseEmitter.event().name("tool_call").data(
                            JSON.toJSONString(Map.of("status", "calling", "message", "正在调用工具..."))));
                } catch (IOException ignored) {}
            }

            @Override
            public void onDone(String fullContent, List<ToolCall> toolCalls) {
                if (!isToolCall || toolCalls == null || toolCalls.isEmpty()) {
                    // === 正常内容回复: 流式输出完毕,保存并结束 ===
                    AiConversationMessage assistantMsg = new AiConversationMessage();
                    assistantMsg.setConversationId(context.getConversationId());
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(fullContent);
                    assistantMsg.setTokenCount(memoryManager.estimateTokens(fullContent));
                    assistantMsg.setCreateTime(new Date());
                    callback.onAssistantMessage(assistantMsg, summary);

                    try {
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                        emitter.complete();
                    } catch (IOException ignored) {}
                    return;
                }

                // === 工具调用: 执行工具 → 注入结果 → 递归 ===
                try {
                    // 5.1 保存 assistant 消息(含 tool_calls JSON)
                    AiConversationMessage assistantMsg = new AiConversationMessage();
                    assistantMsg.setConversationId(context.getConversationId());
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(null);
                    assistantMsg.setToolCalls(toolCallsToJson(toolCalls));
                    assistantMsg.setTokenCount(0);
                    assistantMsg.setCreateTime(new Date());
                    callback.onAssistantMessage(assistantMsg, summary);

                    // 5.2 向 messages 追加 assistant(tool_calls)
                    promptBuilder.addAssistantToolCallMessage(messages, toolCalls);

                    // 5.3 依次执行每个工具
                    for (ToolCall tc : toolCalls) {
                        String result;
                        try {
                            Object execResult = toolRegistry.executeTool(
                                    tc.getFunctionName(), tc.getArgumentsJson());
                            result = execResult != null ? execResult.toString() : "[工具执行完成,无返回内容]";
                        } catch (Exception e) {
                            result = "[工具执行失败] " + tc.getFunctionName() + ": " + e.getMessage();
                        }

                        // 截断过长结果(防止超出上下文限制)
                        if (result.length() > 2000) {
                            result = result.substring(0, 1997) + "...";
                        }

                        // 5.4 发送工具结果事件
                        try {
                            Map<String, String> resultData = new LinkedHashMap<>();
                            resultData.put("code", tc.getFunctionName());
                            resultData.put("result", result);
                            emitter.send(SseEmitter.event().name("tool_result")
                                    .data(JSON.toJSONString(resultData)));
                        } catch (IOException ignored) {}

                        // 5.5 保存 tool 消息到DB
                        AiConversationMessage toolMsg = new AiConversationMessage();
                        toolMsg.setConversationId(context.getConversationId());
                        toolMsg.setRole("tool");
                        toolMsg.setContent(result);
                        toolMsg.setMetadata(JSON.toJSONString(Map.of(
                                "tool_call_id", tc.getId(),
                                "tool_name", tc.getFunctionName()
                        )));
                        toolMsg.setTokenCount(memoryManager.estimateTokens(result));
                        toolMsg.setCreateTime(new Date());
                        callback.onToolMessage(toolMsg);

                        // 5.6 向 messages 追加 tool result
                        promptBuilder.addToolResultMessage(messages, tc.getId(),
                                tc.getFunctionName(), result);
                    }

                    // 5.7 递归: 把工具结果交给LLM继续推理
                    doToolCallingLoop(context, messages, tools, emitter, callback,
                            summary, loopCount + 1);

                } catch (Exception e) {
                    sendError(emitter, "工具调用循环异常: " + e.getMessage());
                }
            }
        });
    }

    // ======================== 辅助方法 ========================

    /** 将 ToolCall 列表序列化为JSON字符串(存入 messages.tool_calls 字段) */
    private String toolCallsToJson(List<ToolCall> toolCalls) {
        JSONArray arr = new JSONArray();
        for (ToolCall tc : toolCalls) {
            JSONObject obj = new JSONObject();
            obj.put("id", tc.getId());
            obj.put("type", tc.getType() != null ? tc.getType() : "function");
            JSONObject func = new JSONObject();
            func.put("name", tc.getFunctionName());
            func.put("arguments", tc.getArgumentsJson());
            obj.put("function", func);
            arr.add(obj);
        }
        return arr.toJSONString();
    }

    private void sendError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(msg));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    // ======================== 公共访问器 ========================

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    // ======================== 内部回调接口 ========================

    /**
     * Agent回调接口 — Phase 4.3 增加 onToolMessage
     */
    public interface AgentCallback {
        /** 用户消息已创建 */
        void onUserMessage(AiConversationMessage userMsg);

        /** 助手消息已生成(含完整内容,或含 tool_calls) */
        void onAssistantMessage(AiConversationMessage assistantMsg, String summary);

        /** 工具执行结果消息 (Phase 4.3) */
        default void onToolMessage(AiConversationMessage toolMsg) {
            // 默认空实现,向后兼容
        }
    }
}
