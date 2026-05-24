package com.ruoyi.system.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * LLM大模型配置查询对象
 *
 * @author ruoyi
 */
public class AiLlmConfigQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 配置名称 */
    private String configName;

    /** 服务商代码 */
    private String provider;

    /** 服务商名称 */
    private String providerName;

    /** 状态 */
    private String status;

    /** 是否默认 */
    private String isDefault;

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
}
