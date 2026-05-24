package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI提示词模板对象 ai_prompt_template
 *
 * @author ruoyi
 */
public class AiPromptTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 类型(0系统提示词 1用户提示词) */
    private String templateType;

    /** 场景代码 */
    private String sceneCode;

    /** 场景名称 */
    private String sceneName;

    /** 提示词内容 */
    private String promptContent;

    /** 变量列表(JSON数组) */
    private String variables;

    /** 关联系统提示词ID */
    private Long systemPromptId;

    /** 状态(0启用 1停用) */
    private String status;

    /** 是否内置(0否 1是) */
    private String isBuiltin;

    /** 显示顺序 */
    private Integer sort;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getPromptContent() {
        return promptContent;
    }

    public void setPromptContent(String promptContent) {
        this.promptContent = promptContent;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public Long getSystemPromptId() {
        return systemPromptId;
    }

    public void setSystemPromptId(Long systemPromptId) {
        this.systemPromptId = systemPromptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(String isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "AiPromptTemplate{" +
                "templateId=" + templateId +
                ", templateName='" + templateName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", sceneCode='" + sceneCode + '\'' +
                ", templateType='" + templateType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
