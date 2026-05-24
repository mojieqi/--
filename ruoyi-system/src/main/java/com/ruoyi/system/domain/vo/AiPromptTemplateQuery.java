package com.ruoyi.system.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI提示词模板查询对象
 *
 * @author ruoyi
 */
public class AiPromptTemplateQuery extends BaseEntity {

    /** 模板名称 */
    private String templateName;

    /** 类型(0系统提示词 1用户提示词) */
    private String templateType;

    /** 场景代码 */
    private String sceneCode;

    /** 场景名称 */
    private String sceneName;

    /** 状态(0启用 1停用) */
    private String status;

    /** 是否内置(0否 1是) */
    private String isBuiltin;

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
}
