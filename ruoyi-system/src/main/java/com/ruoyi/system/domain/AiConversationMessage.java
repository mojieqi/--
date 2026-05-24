package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * AI对话消息对象 ai_conversation_message
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiConversationMessage {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 会话ID */
    private Long conversationId;

    /** 角色(system/user/assistant/tool) */
    private String role;

    /** 消息内容 */
    private String content;

    /** Token数量 */
    private Integer tokenCount;

    /** 工具调用(JSON) */
    private String toolCalls;

    /** 元数据(含知识库引用等) */
    private String metadata;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
    public String getToolCalls() { return toolCalls; }
    public void setToolCalls(String toolCalls) { this.toolCalls = toolCalls; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "AiConversationMessage{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", role='" + role + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' +
                '}';
    }
}
