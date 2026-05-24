package com.ruoyi.system.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.AiTool;
import com.ruoyi.common.utils.StringUtils;

import java.util.Map;

/**
 * 工具抽象基类
 *
 * 提供工具元数据读取、参数校验参数等公共能力。
 * 子类只需实现 {@link #execute(Map)} 具体逻辑即可。
 *
 * Phase 4.2: 定义抽象基类 + 反射实例化支持
 * Phase 4.4: 内置工具继承此类
 * Phase 4.5+: 第三方工具可自定义校验规则
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public abstract class AbstractTool implements ToolHandler {

    /** DB中工具元数据 */
    protected final AiTool toolMeta;

    public AbstractTool(AiTool toolMeta) {
        this.toolMeta = toolMeta;
    }

    @Override
    public String getCode() {
        return toolMeta.getToolCode();
    }

    @Override
    public String getDescription() {
        return toolMeta.getToolDesc();
    }

    /** 获取完整元数据 */
    public AiTool getToolMeta() {
        return toolMeta;
    }

    /** 获取 Function Calling Schema JSON 字符串 */
    public String getFunctionSchema() {
        return toolMeta.getFunctionSchema();
    }

    /** 获取工具名称 */
    public String getToolName() {
        return toolMeta.getToolName();
    }

    /**
     * 校验必要参数是否存在
     * 子类可覆盖此方法实现自定义校验
     *
     * @param arguments 传入参数
     * @param requiredParams 必要参数键名数组
     * @throws IllegalArgumentException 参数缺失时抛出
     */
    protected void requireParams(Map<String, Object> arguments, String... requiredParams) {
        if (requiredParams == null || requiredParams.length == 0) return;
        for (String param : requiredParams) {
            if (!arguments.containsKey(param) || arguments.get(param) == null
                    || (arguments.get(param) instanceof String && StringUtils.isEmpty((String) arguments.get(param)))) {
                throw new IllegalArgumentException(
                    "工具 [" + getCode() + "] 缺少必要参数: " + param);
            }
        }
    }

    /**
     * 从arguments中安全获取字符串参数
     */
    protected String getStringParam(Map<String, Object> arguments, String key) {
        Object val = arguments.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * 从arguments中安全获取整数参数
     */
    protected Integer getIntParam(Map<String, Object> arguments, String key) {
        Object val = arguments.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public abstract Object execute(Map<String, Object> arguments);
}
