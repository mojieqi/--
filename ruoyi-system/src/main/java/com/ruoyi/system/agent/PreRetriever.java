package com.ruoyi.system.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.agent.tool.ToolSpringContext;
import com.ruoyi.system.service.IEmbeddingService;
import com.ruoyi.system.service.IEmbeddingService.ChunkSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

/**
 * 预检索器 — 在 LLM 看到用户消息之前，自动并行执行联网搜索和知识库检索。
 *
 * 核心理念：不依赖 LLM 的 Function Calling 决策，代码层保证每次对话都有搜索数据注入。
 * 搜索失败或超时不阻塞主流程，降级为空结果。
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public class PreRetriever {

    private static final Logger log = LoggerFactory.getLogger(PreRetriever.class);

    private static final String TAVILY_URL = "https://api.tavily.com/search";
    private static final int DEFAULT_MAX_RESULTS = 5;

    /** 联网搜索超时(秒) */
    private static final int WEB_TIMEOUT_SECONDS = 8;

    /** 知识库搜索超时(秒) */
    private static final int KB_TIMEOUT_SECONDS = 5;

    /** 联网搜索结果最大字符数 */
    private static final int MAX_WEB_CHARS = 1500;

    /** 知识库搜索结果最大字符数 */
    private static final int MAX_KB_CHARS = 1500;

    /** 并行搜索用的线程池 */
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "pre-retrieve");
        t.setDaemon(true);
        return t;
    });

    /**
     * 执行预检索：并行执行联网搜索 + 知识库检索，返回格式化的上下文文本。
     *
     * @param query 用户当前问题
     * @param kbId  关联的知识库ID（可为 null，null 时跳过知识库检索）
     * @return 格式化的预检索结果，失败时返回空字符串
     */
    public String preRetrieve(String query, Long kbId) {
        log.info("PreRetriever 开始并行检索: query={}, kbId={}", query, kbId);

        CompletableFuture<String> webFuture = CompletableFuture.supplyAsync(
                () -> searchWeb(query), executor);
        CompletableFuture<String> kbFuture = CompletableFuture.supplyAsync(
                () -> searchKb(query, kbId), executor);

        String webResult = getWithTimeout(webFuture, WEB_TIMEOUT_SECONDS, "联网搜索");
        String kbResult = getWithTimeout(kbFuture, KB_TIMEOUT_SECONDS, "知识库检索");

        return buildContext(webResult, kbResult);
    }

    // ======================== 联网搜索 ========================

    private String searchWeb(String query) {
        try {
            String apiKey = ToolSpringContext.getTavilyApiKey();
            RestTemplate rt = ToolSpringContext.getRestTemplate();

            JSONObject reqBody = new JSONObject();
            reqBody.put("api_key", apiKey);
            reqBody.put("query", query);
            reqBody.put("search_depth", "basic");
            reqBody.put("max_results", DEFAULT_MAX_RESULTS);
            reqBody.put("include_answer", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(reqBody.toJSONString(), headers);

            ResponseEntity<String> response = rt.exchange(
                    TAVILY_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return formatWebResults(query, response.getBody());
            } else {
                log.warn("PreRetriever 联网搜索返回异常状态码: {}", response.getStatusCode());
                return "";
            }
        } catch (IllegalStateException e) {
            log.debug("PreRetriever Tavily API Key 未配置，跳过联网搜索");
            return "";
        } catch (Exception e) {
            log.warn("PreRetriever 联网搜索异常: {}", e.getMessage());
            return "";
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private String formatWebResults(String query, String responseBody) {
        try {
            JSONObject tavilyResp = JSON.parseObject(responseBody);
            StringBuilder sb = new StringBuilder();

            // Tavily AI 摘要
            String answer = tavilyResp.getString("answer");
            if (answer != null && !answer.isEmpty()) {
                sb.append("▸ 摘要: ").append(answer).append("\n\n");
            }

            // 搜索结果列表
            JSONArray results = tavilyResp.getJSONArray("results");
            if (results != null && !results.isEmpty()) {
                sb.append("▸ 搜索链接:\n");
                int count = 0;
                for (int i = 0; i < results.size() && sb.length() < MAX_WEB_CHARS; i++) {
                    JSONObject r = results.getJSONObject(i);
                    String title = r.getString("title");
                    String url = r.getString("url");
                    String content = r.getString("content");

                    if (title == null) continue;
                    count++;
                    sb.append("  ").append(count).append(". ");
                    sb.append(title);
                    if (url != null) sb.append(" (").append(url).append(")");
                    sb.append("\n");
                    if (content != null) {
                        int maxSnippet = Math.min(content.length(), 200);
                        sb.append("     ").append(content, 0, maxSnippet).append("...\n");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("PreRetriever 解析联网搜索结果异常: {}", e.getMessage());
            return "";
        }
    }

    // ======================== 知识库检索 ========================

    private String searchKb(String query, Long kbId) {
        if (kbId == null) return "";

        try {
            IEmbeddingService embeddingService = ToolSpringContext.getEmbeddingService();
            List<ChunkSearchResult> results = embeddingService.searchInBase(kbId, query, 3);

            if (results == null || results.isEmpty()) return "";

            return formatKbResults(results);
        } catch (IllegalStateException e) {
            log.debug("PreRetriever EmbeddingService 未就绪，跳过知识库检索");
            return "";
        } catch (Exception e) {
            log.warn("PreRetriever 知识库检索异常: {}", e.getMessage());
            return "";
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private String formatKbResults(List<ChunkSearchResult> results) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size() && sb.length() < MAX_KB_CHARS; i++) {
            ChunkSearchResult r = results.get(i);
            sb.append("▸ 资料 ").append(i + 1);
            if (r.getSimilarityScore() != null) {
                sb.append(" (相关度: ").append(String.format("%.1f%%", r.getSimilarityScore() * 100)).append(")");
            }
            sb.append(":\n  ");

            String content = r.getChunkContent();
            if (content != null) {
                if (content.length() > 400) {
                    sb.append(content, 0, 400).append("...");
                } else {
                    sb.append(content);
                }
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    // ======================== 结果组装 ========================

    private String buildContext(String webResult, String kbResult) {
        boolean hasWeb = webResult != null && !webResult.isEmpty();
        boolean hasKb = kbResult != null && !kbResult.isEmpty();

        if (!hasWeb && !hasKb) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【自动检索结果 — 回答前请优先参考以下信息】\n");

        if (hasWeb) {
            sb.append("\n## 联网搜索\n");
            sb.append(webResult);
        }
        if (hasKb) {
            sb.append("## 知识库资料\n");
            sb.append(kbResult);
        }

        sb.append("请在回答中优先使用以上检索信息。如果检索结果不足以回答用户问题，再结合你的知识进行补充。\n");
        return sb.toString();
    }

    // ======================== 工具方法 ========================

    private String getWithTimeout(Future<String> future, int timeoutSeconds, String label) {
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("PreRetriever {} 超时({}s)", label, timeoutSeconds);
            return "";
        } catch (Exception e) {
            log.warn("PreRetriever {} 异常: {}", label, e.getMessage());
            return "";
        }
    }
}
