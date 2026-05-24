package com.ruoyi.system.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.AiTool;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心
 *
 * 核心职责:
 * 1. 从DB加载所有启用工具,通过反射实例化 ToolHandler
 * 2. 提供按工具代码查找和执行的统一入口
 * 3. 生成 tools JSON 数组 (OpenAI Function Calling 格式) — Phase 4.3 使用
 * 4. 生成工具描述文本 — 注入系统提示词
 *
 * Phase 4.2: 注册表框架 (加载+查询+执行+JSON构建)
 * Phase 4.3: buildToolsJson() 对接 LlmCaller 的 tools 参数
 * Phase 4.4: 4个内置工具类被实例化并注册
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    /** 工具注册表: toolCode → ToolHandler */
    private final Map<String, ToolHandler> registry = new ConcurrentHashMap<>();

    /** 原始工具元数据列表 (保留用于 rebuild 和描述生成) */
    private final List<AiTool> toolMetaList;

    /** 加载失败的工具代码集合 (避免重复报错) */
    private final Set<String> failedCodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 构造注册中心并立即加载工具
     *
     * @param enabledTools 启用的工具元数据列表 (来自 IAiToolService.selectEnabledTools())
     */
    public ToolRegistry(List<AiTool> enabledTools) {
        this.toolMetaList = enabledTools != null ? enabledTools : Collections.emptyList();
        loadTools();
    }

    // ======================== 工具加载 ========================

    /**
     * 从元数据列表实例化并注册所有工具
     * 加载失败的工具记录到 failedCodes,不阻塞其他工具
     */
    private void loadTools() {
        registry.clear();
        failedCodes.clear();

        for (AiTool toolMeta : toolMetaList) {
            if (!"0".equals(toolMeta.getStatus())) {
                log.debug("工具 [{}] 状态非启用,跳过注册", toolMeta.getToolCode());
                continue;
            }
            try {
                ToolHandler handler = instantiateHandler(toolMeta);
                registry.put(toolMeta.getToolCode(), handler);
                log.info("工具注册成功: code={}, class={}", toolMeta.getToolCode(), toolMeta.getHandlerClass());
            } catch (ClassNotFoundException e) {
                failedCodes.add(toolMeta.getToolCode());
                log.warn("工具 [{}] 处理器类未找到: {} (将在 Phase 4.4 实现后自动启用)",
                        toolMeta.getToolCode(), toolMeta.getHandlerClass());
            } catch (Exception e) {
                failedCodes.add(toolMeta.getToolCode());
                log.error("工具 [{}] 注册失败: {} - {}", toolMeta.getToolCode(),
                        toolMeta.getHandlerClass(), e.getMessage());
            }
        }

        log.info("ToolRegistry 初始化完成: 已注册 {}/{} 个工具",
                registry.size(), toolMetaList.size());
    }

    /**
     * 通过反射实例化工具处理器
     * 要求处理器类有公共构造函数: public XxxTool(AiTool toolMeta)
     */
    private ToolHandler instantiateHandler(AiTool toolMeta) throws Exception {
        String className = toolMeta.getHandlerClass();
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException("handlerClass 为空,无法实例化");
        }
        Class<?> clazz = Class.forName(className);
        return (ToolHandler) clazz.getConstructor(AiTool.class).newInstance(toolMeta);
    }

    // ======================== 工具查询 ========================

    /** 按代码获取工具处理器 */
    public ToolHandler getTool(String code) {
        return registry.get(code);
    }

    /** 检查工具是否已注册 */
    public boolean hasTool(String code) {
        return registry.containsKey(code);
    }

    /** 已注册工具数量 */
    public int getRegisteredCount() {
        return registry.size();
    }

    /** 是否存在任何已注册工具 */
    public boolean hasAnyTools() {
        return !registry.isEmpty();
    }

    /** 获取所有已注册工具代码 */
    public Set<String> getToolCodes() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    // ======================== 工具执行 ========================

    /**
     * 执行指定工具
     *
     * @param code      工具代码
     * @param arguments 函数参数
     * @return 工具执行结果
     * @throws RuntimeException 工具未注册或执行失败
     */
    public Object executeTool(String code, Map<String, Object> arguments) {
        ToolHandler handler = registry.get(code);
        if (handler == null) {
            if (failedCodes.contains(code)) {
                throw new RuntimeException("工具 [" + code + "] 加载失败(处理器类缺失),无法执行");
            }
            throw new RuntimeException("工具 [" + code + "] 未注册,可用工具: " + registry.keySet());
        }
        try {
            return handler.execute(arguments);
        } catch (Exception e) {
            log.error("工具 [{}] 执行异常: {}", code, e.getMessage());
            throw new RuntimeException("工具 [" + code + "] 执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行指定工具 (JSON参数字符串格式)
     * Phase 4.3 对接: 解析 LLM 返回的 tool_calls arguments JSON
     */
    public Object executeTool(String code, String argumentsJson) {
        Map<String, Object> args;
        try {
            if (StringUtils.isEmpty(argumentsJson)) {
                args = Collections.emptyMap();
            } else {
                args = JSON.parseObject(argumentsJson);
            }
        } catch (Exception e) {
            throw new RuntimeException("工具 [" + code + "] 参数JSON解析失败: " + e.getMessage(), e);
        }
        return executeTool(code, args);
    }

    // ======================== tools JSON 构建 (Phase 4.3) ========================

    /**
     * 构建 OpenAI Function Calling 的 tools 参数 JSON
     *
     * 格式:
     * [
     *   {
     *     "type": "function",
     *     "function": {
     *       "name": "web_search",
     *       "description": "联网搜索最新信息",
     *       "parameters": { "type": "object", "properties": {...}, "required": [...] }
     *     }
     *   }
     * ]
     *
     * Phase 4.3: LlmCaller.chatStream() 将此数组作为 tools 参数发送
     */
    public JSONArray buildToolsJson() {
        JSONArray tools = new JSONArray();
        for (AiTool toolMeta : toolMetaList) {
            if (!registry.containsKey(toolMeta.getToolCode())) {
                continue; // 跳过未成功注册的
            }

            JSONObject toolObj = new JSONObject();
            toolObj.put("type", "function");

            JSONObject function = new JSONObject();
            function.put("name", toolMeta.getToolCode());
            function.put("description",
                    StringUtils.isNotEmpty(toolMeta.getToolDesc()) ? toolMeta.getToolDesc() : toolMeta.getToolName());

            // 解析 functionSchema JSON → 提取 parameters (兼容两种格式)
            if (StringUtils.isNotEmpty(toolMeta.getFunctionSchema())) {
                try {
                    JSONObject schema = JSON.parseObject(toolMeta.getFunctionSchema());
                    // 完整格式: {"name":"...","description":"...","parameters":{...}}
                    // 纯parameters格式: {"type":"object","properties":{...}}
                    if (schema.containsKey("parameters")) {
                        function.put("parameters", schema.get("parameters"));
                    } else {
                        // 兼容无包装的纯 parameters 格式
                        function.put("parameters", schema);
                    }
                } catch (Exception e) {
                    log.error("工具 [{}] functionSchema JSON解析失败: {}", toolMeta.getToolCode(), e.getMessage());
                    function.put("parameters", buildEmptyParameters());
                }
            } else {
                function.put("parameters", buildEmptyParameters());
            }

            toolObj.put("function", function);
            tools.add(toolObj);
        }
        return tools;
    }

    /**
     * 构建空的 parameters schema (没有定义 schema 时的降级方案)
     */
    private JSONObject buildEmptyParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");
        params.put("properties", new JSONObject());
        return params;
    }

    // ======================== 工具描述文本 (系统提示词) ========================

    /**
     * 生成工具列表描述文本,用于注入到系统提示词
     *
     * Phase 4.3 可选: 如果 LLM 不支持 Function Calling,用此描述告知 LLM 工具能力
     */
    public String buildToolDescription() {
        if (registry.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n## 可用工具\n");
        sb.append("你可以使用以下工具来辅助回答用户问题:\n\n");

        for (AiTool toolMeta : toolMetaList) {
            if (!registry.containsKey(toolMeta.getToolCode())) continue;
            sb.append("- **");
            sb.append(toolMeta.getToolCode());
            sb.append("**");
            if (StringUtils.isNotEmpty(toolMeta.getToolName())) {
                sb.append(" (").append(toolMeta.getToolName()).append(")");
            }
            sb.append(": ");
            sb.append(toolMeta.getToolDesc());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
