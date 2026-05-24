package com.ruoyi.system.domain;

/**
 * AI提示词生成记录对象 ai_prompt_generation
 *
 * @author ruoyi
 */
public class AiPromptGeneration {

    /** 生成ID */
    private Long genId;

    /** 用户输入的需求描述 */
    private String userInput;

    /** AI生成的提示词 */
    private String generatedPrompt;

    /** 生成类型(0系统提示词 1用户提示词) */
    private String templateType;

    /** 使用的LLM配置ID */
    private Long llmConfigId;

    /** 使用的模型名称 */
    private String modelName;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private java.util.Date createTime;

    public Long getGenId() {
        return genId;
    }

    public void setGenId(Long genId) {
        this.genId = genId;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getGeneratedPrompt() {
        return generatedPrompt;
    }

    public void setGeneratedPrompt(String generatedPrompt) {
        this.generatedPrompt = generatedPrompt;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Long getLlmConfigId() {
        return llmConfigId;
    }

    public void setLlmConfigId(Long llmConfigId) {
        this.llmConfigId = llmConfigId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }
}
