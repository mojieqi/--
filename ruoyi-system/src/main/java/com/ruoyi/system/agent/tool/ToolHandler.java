package com.ruoyi.system.agent.tool;

import java.util.Map;

/**
 * 工具处理器接口 — 所有AI工具必须实现此接口
 *
 * Phase 4.2: 定义工具契约
 * Phase 4.4: 内置工具实现此接口 (WebSearchTool, KbQueryTool, DateCalcTool, ContentAuditTool)
 * Phase 4.3: ToolRegistry 通过此接口执行工具,将结果注入LLM上下文
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ToolHandler {

    /**
     * 获取工具唯一代码 (如 web_search, kb_query)
     */
    String getCode();

    /**
     * 获取工具功能描述文本
     * 用于构建系统提示词中的工具列表
     */
    String getDescription();

    /**
     * 执行工具
     *
     * @param arguments 函数参数 (LLM生成的参数键值对)
     * @return 执行结果 (字符串或Map, ToolRegistry统一序列化为JSON字符串注入上下文)
     */
    Object execute(Map<String, Object> arguments);
}
