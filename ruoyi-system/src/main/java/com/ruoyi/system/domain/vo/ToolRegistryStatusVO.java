package com.ruoyi.system.domain.vo;

/**
 * 工具注册状态VO — Phase 4.5 前后端联动
 *
 * 用于 GET /ai/tool/registry-status 返回每个工具在 ToolRegistry 中的运行时状态
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class ToolRegistryStatusVO {

    /** 工具ID */
    private Long toolId;

    /** 工具代码 */
    private String toolCode;

    /** 工具名称 */
    private String toolName;

    /** 处理器类全限定名 */
    private String handlerClass;

    /** 是否内置(0否 1是) */
    private String isBuiltin;

    /** DB状态(0启用 1停用) */
    private String status;

    /**
     * 注册状态:
     *   REGISTERED   — 类存在，可被 ToolRegistry 实例化
     *   CLASS_MISSING — 处理器类在 classpath 中找不到
     *   DISABLED     — DB中已停用
     */
    private String registryStatus;

    /** 错误信息(仅 CLASS_MISSING 时有值) */
    private String errorMsg;

    // ---- constructors ----

    public ToolRegistryStatusVO() {}

    public ToolRegistryStatusVO(Long toolId, String toolCode, String toolName, String handlerClass,
                                 String isBuiltin, String status, String registryStatus, String errorMsg) {
        this.toolId = toolId;
        this.toolCode = toolCode;
        this.toolName = toolName;
        this.handlerClass = handlerClass;
        this.isBuiltin = isBuiltin;
        this.status = status;
        this.registryStatus = registryStatus;
        this.errorMsg = errorMsg;
    }

    // ---- getters & setters ----

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public String getToolCode() { return toolCode; }
    public void setToolCode(String toolCode) { this.toolCode = toolCode; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getHandlerClass() { return handlerClass; }
    public void setHandlerClass(String handlerClass) { this.handlerClass = handlerClass; }

    public String getIsBuiltin() { return isBuiltin; }
    public void setIsBuiltin(String isBuiltin) { this.isBuiltin = isBuiltin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegistryStatus() { return registryStatus; }
    public void setRegistryStatus(String registryStatus) { this.registryStatus = registryStatus; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
