package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI对话会话对象 ai_conversation
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiConversation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long conversationId;

    /** 会话标题 */
    private String title;

    /** 使用的LLM配置ID */
    private Long llmConfigId;

    /** 使用的提示词模板ID */
    private Long promptTemplateId;

    /** 关联知识库ID */
    private Long kbId;

    /** 记忆模式(0无记忆 1短期记忆 2长期记忆) */
    private String memoryMode;

    /** 上下文最大Token数 */
    private Integer maxTokens;

    /** 消息数量 */
    private Integer messageCount;

    /** 状态(0进行中 1已完成) */
    private String status;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getLlmConfigId() { return llmConfigId; }
    public void setLlmConfigId(Long llmConfigId) { this.llmConfigId = llmConfigId; }
    public Long getPromptTemplateId() { return promptTemplateId; }
    public void setPromptTemplateId(Long promptTemplateId) { this.promptTemplateId = promptTemplateId; }
    public Long getKbId() { return kbId; }
    public void setKbId(Long kbId) { this.kbId = kbId; }
    public String getMemoryMode() { return memoryMode; }
    public void setMemoryMode(String memoryMode) { this.memoryMode = memoryMode; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "AiConversation{" +
                "conversationId=" + conversationId +
                ", title='" + title + '\'' +
                ", llmConfigId=" + llmConfigId +
                ", promptTemplateId=" + promptTemplateId +
                ", kbId=" + kbId +
                ", memoryMode='" + memoryMode + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
