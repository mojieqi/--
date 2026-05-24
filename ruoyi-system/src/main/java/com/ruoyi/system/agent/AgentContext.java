package com.ruoyi.system.agent;

import com.ruoyi.system.domain.AiConversationMessage;
import java.util.List;

/**
 * Agent上下文 — 封装每次对话请求的完整上下文
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AgentContext {

    /** 会话ID */
    private final Long conversationId;

    /** 系统提示词内容 */
    private final String systemPrompt;

    /** 历史消息(已按时间排序) */
    private final List<AiConversationMessage> history;

    /** 知识库检索上下文(RAG) */
    private final String knowledgeContext;

    /** 用户当前输入 */
    private final String userMessage;

    /** LLM配置ID */
    private final Long llmConfigId;

    /** 模型名称 */
    private final String modelName;

    /** API Base URL */
    private final String baseUrl;

    /** API Key */
    private final String apiKey;

    /** 最大Token数 */
    private final Integer maxTokens;

    /** 温度参数 */
    private final Double temperature;

    private AgentContext(Builder builder) {
        this.conversationId = builder.conversationId;
        this.systemPrompt = builder.systemPrompt;
        this.history = builder.history;
        this.knowledgeContext = builder.knowledgeContext;
        this.userMessage = builder.userMessage;
        this.llmConfigId = builder.llmConfigId;
        this.modelName = builder.modelName;
        this.baseUrl = builder.baseUrl;
        this.apiKey = builder.apiKey;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
    }

    public Long getConversationId() { return conversationId; }
    public String getSystemPrompt() { return systemPrompt; }
    public List<AiConversationMessage> getHistory() { return history; }
    public String getKnowledgeContext() { return knowledgeContext; }
    public String getUserMessage() { return userMessage; }
    public Long getLlmConfigId() { return llmConfigId; }
    public String getModelName() { return modelName; }
    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public Integer getMaxTokens() { return maxTokens; }
    public Double getTemperature() { return temperature; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long conversationId;
        private String systemPrompt;
        private List<AiConversationMessage> history;
        private String knowledgeContext;
        private String userMessage;
        private Long llmConfigId;
        private String modelName;
        private String baseUrl;
        private String apiKey;
        private Integer maxTokens = 4096;
        private Double temperature = 0.7;

        public Builder conversationId(Long v) { this.conversationId = v; return this; }
        public Builder systemPrompt(String v) { this.systemPrompt = v; return this; }
        public Builder history(List<AiConversationMessage> v) { this.history = v; return this; }
        public Builder knowledgeContext(String v) { this.knowledgeContext = v; return this; }
        public Builder userMessage(String v) { this.userMessage = v; return this; }
        public Builder llmConfigId(Long v) { this.llmConfigId = v; return this; }
        public Builder modelName(String v) { this.modelName = v; return this; }
        public Builder baseUrl(String v) { this.baseUrl = v; return this; }
        public Builder apiKey(String v) { this.apiKey = v; return this; }
        public Builder maxTokens(Integer v) { this.maxTokens = v; return this; }
        public Builder temperature(Double v) { this.temperature = v; return this; }

        public AgentContext build() { return new AgentContext(this); }
    }
}
