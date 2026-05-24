package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiConversationMessage;
import java.util.List;

/**
 * Agent核心Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiAgentService {

    /**
     * 发送消息 — 流式SSE对话
     *
     * @param conversationId 会话ID
     * @param userMessage    用户消息内容
     * @return SseEmitter 流式发射器
     */
    org.springframework.web.servlet.mvc.method.annotation.SseEmitter chat(Long conversationId, String userMessage);

    /**
     * 获取会话消息列表
     */
    List<AiConversationMessage> getMessages(Long conversationId);

    /**
     * 保存消息
     */
    int saveMessage(AiConversationMessage message);
}
