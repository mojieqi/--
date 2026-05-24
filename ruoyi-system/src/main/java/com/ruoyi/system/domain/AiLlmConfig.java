package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * LLM大模型配置对象 ai_llm_config
 *
 * @author ruoyi
 */
public class AiLlmConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 配置名称 */
    private String configName;

    /** 服务商代码 */
    private String provider;

    /** 服务商显示名称 */
    private String providerName;

    /** API BaseURL */
    private String baseUrl;

    /** API密钥(加密存储) */
    private String apiKey;

    /** 默认模型 */
    private String defaultModel;

    /** 状态(0正常 1停用) */
    private String status;

    /** 是否默认(0否 1是) */
    private String isDefault;

    /** 显示顺序 */
    private Integer sort;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "AiLlmConfig{" +
                "configId=" + configId +
                ", configName='" + configName + '\'' +
                ", provider='" + provider + '\'' +
                ", providerName='" + providerName + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", defaultModel='" + defaultModel + '\'' +
                ", status='" + status + '\'' +
                ", isDefault='" + isDefault + '\'' +
                ", sort=" + sort +
                '}';
    }
}
