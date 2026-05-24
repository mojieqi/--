package com.ruoyi.system.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.AiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 联网搜索工具 — 通过 Tavily Search API 检索互联网实时信息
 *
 * Tavily API 文档: https://docs.tavily.com/
 * 接口: POST https://api.tavily.com/search
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class WebSearchTool extends AbstractTool {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);

    private static final String TAVILY_URL = "https://api.tavily.com/search";
    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final int MAX_CONTENT_CHARS = 3000;

    public WebSearchTool(AiTool toolMeta) {
        super(toolMeta);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        requireParams(arguments, "query");
        String query = getStringParam(arguments, "query");

        log.info("WebSearchTool 开始搜索: query={}", query);

        try {
            String apiKey = ToolSpringContext.getTavilyApiKey();
            RestTemplate rt = ToolSpringContext.getRestTemplate();

            // 构建 Tavily 请求体
            JSONObject reqBody = new JSONObject();
            reqBody.put("api_key", apiKey);
            reqBody.put("query", query);
            reqBody.put("search_depth", "basic");
            reqBody.put("max_results", DEFAULT_MAX_RESULTS);
            reqBody.put("include_answer", true);

            // 构建 HTTP 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(reqBody.toJSONString(), headers);

            // 调用 Tavily API
            ResponseEntity<String> response = rt.exchange(
                    TAVILY_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return formatResults(query, response.getBody());
            } else {
                return JSON.toJSONString(Collections.singletonMap("error",
                        "Tavily API 返回异常状态码: " + response.getStatusCode()));
            }
        } catch (IllegalStateException e) {
            log.error("WebSearchTool Tavily API Key 未配置: {}", e.getMessage());
            return JSON.toJSONString(Collections.singletonMap("error",
                    "联网搜索未配置 Tavily API Key，请在 application.yml 中设置 campus.tavily.api-key"));
        } catch (Exception e) {
            log.error("WebSearchTool 搜索异常: {}", e.getMessage());
            return JSON.toJSONString(Collections.singletonMap("error",
                    "搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 格式化 Tavily 搜索结果
     */
    private String formatResults(String query, String responseBody) {
        try {
            JSONObject tavilyResp = JSON.parseObject(responseBody);

            StringBuilder sb = new StringBuilder();
            sb.append("搜索查询: ").append(query).append("\n\n");

            // Tavily 的 AI 摘要
            String answer = tavilyResp.getString("answer");
            if (answer != null && !answer.isEmpty()) {
                sb.append("摘要: ").append(answer).append("\n\n");
            }

            // 搜索结果列表
            JSONArray results = tavilyResp.getJSONArray("results");
            if (results != null && !results.isEmpty()) {
                sb.append("搜索结果:\n");
                int count = 0;
                for (int i = 0; i < results.size(); i++) {
                    JSONObject r = results.getJSONObject(i);
                    String title = r.getString("title");
                    String url = r.getString("url");
                    String content = r.getString("content");

                    if (title == null) continue;
                    count++;

                    sb.append(count).append(". **").append(title).append("**\n");
                    sb.append("   链接: ").append(url != null ? url : "无").append("\n");
                    if (content != null) {
                        String snippet = content.length() > 400 ? content.substring(0, 400) + "..." : content;
                        sb.append("   内容: ").append(snippet).append("\n");
                    }
                    sb.append("\n");

                    // 限制总输出长度
                    if (sb.length() > MAX_CONTENT_CHARS && i < results.size() - 1) {
                        sb.append("...(更多结果已省略)\n");
                        break;
                    }
                }
            } else {
                sb.append("未找到相关结果。\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("WebSearchTool 解析结果异常: {}", e.getMessage());
            return "搜索完成，但结果解析失败: " + e.getMessage() + "\n原始响应: " + responseBody;
        }
    }
}
