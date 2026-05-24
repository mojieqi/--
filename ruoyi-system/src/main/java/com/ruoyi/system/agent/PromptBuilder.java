package com.ruoyi.system.agent;

import com.ruoyi.system.agent.LlmCaller.ToolCall;
import com.ruoyi.system.domain.AiConversationMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 提示词构建器 — 组装发送给LLM的完整消息数组
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class PromptBuilder {

    /**
     * 构建完整的消息数组 (OpenAI兼容格式)
     *
     * @param context Agent上下文
     * @param summary 早期对话的摘要(可为null)
     * @return JSONArray 消息数组
     */
    public JSONArray buildMessages(AgentContext context, String summary) {
        JSONArray messages = new JSONArray();

        // 1. 系统提示词
        buildSystemMessage(messages, context, summary);

        // 2. 知识库上下文(作为系统消息的补充)
        if (context.getKnowledgeContext() != null && !context.getKnowledgeContext().isEmpty()) {
            buildKnowledgeContext(messages, context.getKnowledgeContext());
        }

        // 3. 摘要(如果存在)
        if (summary != null && !summary.isEmpty()) {
            buildSummaryMessage(messages, summary);
        }

        // 4. 历史消息
        List<AiConversationMessage> history = context.getHistory();
        if (history != null) {
            for (AiConversationMessage msg : history) {
                buildHistoryMessage(messages, msg);
            }
        }

        // 5. 用户当前输入
        buildUserMessage(messages, context.getUserMessage());

        return messages;
    }

    /**
     * 构建用于摘要生成的简单消息数组
     */
    public JSONArray buildSummaryRequest(String earlyMessagesText) {
        JSONArray messages = new JSONArray();

        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一个对话摘要助手。请将对话历史压缩为简洁的摘要。");
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "请将以下对话历史压缩为一段简洁的摘要，保留关键信息、用户偏好和重要事实，不超过200字：\n\n" + earlyMessagesText);
        messages.add(userMsg);

        return messages;
    }

    // --- private helpers ---

    private void buildSystemMessage(JSONArray messages, AgentContext context, String summary) {
        JSONObject msg = new JSONObject();
        msg.put("role", "system");

        StringBuilder content = new StringBuilder();

        // 主系统提示词
        if (context.getSystemPrompt() != null && !context.getSystemPrompt().isEmpty()) {
            content.append(context.getSystemPrompt());
        } else {
            content.append("你是一个AI校园墙智能助手，可以回答校园相关的问题，帮助用户解决校园生活中的各种需求。");
        }

        // 附加早期对话摘要
        if (summary != null && !summary.isEmpty()) {
            content.append("\n\n【历史对话摘要】\n").append(summary);
        }

        msg.put("content", content.toString());
        messages.add(msg);
    }

    private void buildKnowledgeContext(JSONArray messages, String kbContext) {
        JSONObject msg = new JSONObject();
        msg.put("role", "system");
        msg.put("content", "【参考知识库内容】\n\n" + kbContext + "\n\n请基于以上参考内容回答用户问题。如果参考内容不足以回答问题，可以结合你的知识进行补充，但要明确说明。");
        messages.add(msg);
    }

    private void buildSummaryMessage(JSONArray messages, String summary) {
        // 摘要已包含在系统提示词中，此方法预留
    }

    private void buildHistoryMessage(JSONArray messages, AiConversationMessage msg) {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("role", msg.getRole());

        // 处理工具调用消息
        if ("tool".equals(msg.getRole()) && msg.getToolCalls() != null) {
            try {
                jsonMsg.put("content", msg.getContent());
                JSONObject toolCalls = JSONObject.parseObject(msg.getToolCalls());
                if (toolCalls.containsKey("tool_call_id")) {
                    jsonMsg.put("tool_call_id", toolCalls.getString("tool_call_id"));
                }
                if (toolCalls.containsKey("name")) {
                    jsonMsg.put("name", toolCalls.getString("name"));
                }
            } catch (Exception e) {
                jsonMsg.put("content", msg.getContent());
            }
        } else {
            jsonMsg.put("content", msg.getContent());
        }

        messages.add(jsonMsg);
    }

    private void buildUserMessage(JSONArray messages, String userMessage) {
        JSONObject msg = new JSONObject();
        msg.put("role", "user");
        msg.put("content", userMessage);
        messages.add(msg);
    }

    // ======================== 工具调用消息构建 (Phase 4.3) ========================

    /**
     * 向消息数组追加 assistant 角色消息(含 tool_calls)
     * Phase 4.3: 一轮工具调用开始时，先添加 assistant(含tool_calls)
     */
    public void addAssistantToolCallMessage(JSONArray messages, List<ToolCall> toolCalls) {
        JSONObject msg = new JSONObject();
        msg.put("role", "assistant");
        msg.put("content", (String) null);

        JSONArray tcArray = new JSONArray();
        for (ToolCall tc : toolCalls) {
            JSONObject tcObj = new JSONObject();
            tcObj.put("id", tc.getId());
            tcObj.put("type", tc.getType() != null ? tc.getType() : "function");
            JSONObject funcObj = new JSONObject();
            funcObj.put("name", tc.getFunctionName());
            funcObj.put("arguments", tc.getArgumentsJson());
            tcObj.put("function", funcObj);
            tcArray.add(tcObj);
        }
        msg.put("tool_calls", tcArray);
        messages.add(msg);
    }

    /**
     * 向消息数组追加 tool 角色消息(工具执行结果)
     * Phase 4.3: 工具执行完成后，添加 tool 消息供 LLM 继续推理
     */
    public void addToolResultMessage(JSONArray messages, String toolCallId, String toolName, String resultContent) {
        JSONObject msg = new JSONObject();
        msg.put("role", "tool");
        msg.put("tool_call_id", toolCallId);
        msg.put("name", toolName);
        msg.put("content", resultContent);
        messages.add(msg);
    }
}
