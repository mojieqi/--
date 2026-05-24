package com.ruoyi.system.agent.tool;

import com.ruoyi.system.service.IEmbeddingService;
import org.springframework.web.client.RestTemplate;

/**
 * 工具静态服务上下文
 *
 * 解决反射实例化的工具类无法使用 Spring DI 的问题。
 * 在 Agent 引擎初始化前由外部注入所需服务。
 *
 * Phase 4.4: WebSearchTool/KbQueryTool 通过此类获取外部依赖
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class ToolSpringContext {

    private static volatile IEmbeddingService embeddingService;
    private static volatile RestTemplate restTemplate;
    private static volatile String tavilyApiKey;

    private ToolSpringContext() {}

    /** 初始化上下文（在 Agent 引擎创建前调用） */
    public static void init(IEmbeddingService embeddingService, RestTemplate restTemplate, String tavilyApiKey) {
        ToolSpringContext.embeddingService = embeddingService;
        ToolSpringContext.restTemplate = restTemplate;
        ToolSpringContext.tavilyApiKey = tavilyApiKey;
    }

    /** 清除上下文（测试用） */
    public static void clear() {
        embeddingService = null;
        restTemplate = null;
        tavilyApiKey = null;
    }

    public static IEmbeddingService getEmbeddingService() {
        if (embeddingService == null) {
            throw new IllegalStateException("ToolSpringContext 未初始化: embeddingService 为 null");
        }
        return embeddingService;
    }

    public static RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            throw new IllegalStateException("ToolSpringContext 未初始化: restTemplate 为 null");
        }
        return restTemplate;
    }

    public static String getTavilyApiKey() {
        if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
            throw new IllegalStateException("ToolSpringContext 未初始化: tavilyApiKey 为空，请在 application.yml 中配置 campus.tavily.api-key");
        }
        return tavilyApiKey;
    }

    public static boolean isInitialized() {
        return embeddingService != null && restTemplate != null && tavilyApiKey != null && !tavilyApiKey.isEmpty();
    }
}
