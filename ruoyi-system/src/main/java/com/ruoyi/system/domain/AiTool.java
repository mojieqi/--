package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI工具注册表对象 ai_tool
 *
 * @author ruoyi
 */
public class AiTool extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 工具ID */
    private Long toolId;

    /** 工具名称 */
    private String toolName;

    /** 工具代码(web_search/kb_query/date_calc/content_audit) */
    private String toolCode;

    /** 工具描述 */
    private String toolDesc;

    /** Function Calling Schema定义(JSON) */
    private String functionSchema;

    /** 处理器类全限定名 */
    private String handlerClass;

    /** 是否内置(0否 1是) */
    private String isBuiltin;

    /** 状态(0启用 1停用) */
    private String status;

    /** 显示排序 */
    private Integer sort;

    /** 工具ID数组(批量操作使用) */
    private Long[] toolIds;

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolCode() {
        return toolCode;
    }

    public void setToolCode(String toolCode) {
        this.toolCode = toolCode;
    }

    public String getToolDesc() {
        return toolDesc;
    }

    public void setToolDesc(String toolDesc) {
        this.toolDesc = toolDesc;
    }

    public String getFunctionSchema() {
        return functionSchema;
    }

    public void setFunctionSchema(String functionSchema) {
        this.functionSchema = functionSchema;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(String isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Long[] getToolIds() {
        return toolIds;
    }

    public void setToolIds(Long[] toolIds) {
        this.toolIds = toolIds;
    }

    @Override
    public String toString() {
        return "AiTool{" +
                "toolId=" + toolId +
                ", toolName='" + toolName + '\'' +
                ", toolCode='" + toolCode + '\'' +
                ", toolDesc='" + toolDesc + '\'' +
                ", handlerClass='" + handlerClass + '\'' +
                ", isBuiltin='" + isBuiltin + '\'' +
                ", status='" + status + '\'' +
                ", sort=" + sort +
                '}';
    }
}
