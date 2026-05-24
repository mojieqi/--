package com.ruoyi.system.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM调用器 — 负责调用LLM API的流式请求，支持 Function Calling 工具调用
 *
 * Phase 4.3: 新增 tools 参数发送 与 tool_calls 流式解析
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class LlmCaller {

    private final RestTemplate restTemplate;

    public LlmCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ======================== 内部类型 ========================

    /**
     * LLM工具调用描述
     * Phase 4.3: 解析流式 tool_calls 后构建
     */
    public static class ToolCall {
        private final String id;
        private final String type;
        private final String functionName;
        private final String argumentsJson;

        public ToolCall(String id, String type, String functionName, String argumentsJson) {
            this.id = id;
            this.type = type;
            this.functionName = functionName;
            this.argumentsJson = argumentsJson;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getFunctionName() { return functionName; }
        public String getArgumentsJson() { return argumentsJson; }

        @Override
        public String toString() {
            return "ToolCall{id='" + id + "', function='" + functionName + "', args=" + argumentsJson + "}";
        }
    }

    /**
     * 流式回调接口 — 支持内容块和工具调用
     * Phase 4.3: 替代原有 Consumer&lt;String&gt;
     */
    public interface StreamCallback {
        /** 收到内容增量 */
        void onContent(String deltaContent);

        /** 工具调用开始(第一个tool_call chunk到达时触发一次) */
        default void onToolCallStart() {}

        /** 流完成,携带完整内容和工具调用列表 */
        void onDone(String fullContent, List<ToolCall> toolCalls);
    }

    // ======================== 原有方法(保留) ========================

    /**
     * 流式调用LLM API (不含tools,向后兼容)
     */
    public void chatStream(AgentContext context, JSONArray messages, SseEmitter emitter,
                           java.util.function.Consumer<String> onComplete) {
        chatStreamWithTools(context, messages, null, emitter, new StreamCallback() {
            @Override
            public void onContent(String deltaContent) {
                try {
                    emitter.send(SseEmitter.event().name("message").data(deltaContent));
                } catch (IOException ignored) {}
            }

            @Override
            public void onDone(String fullContent, List<ToolCall> toolCalls) {
                try {
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                } catch (IOException ignored) {}
                if (onComplete != null) {
                    onComplete.accept(fullContent);
                }
            }
        });
    }

    // ======================== 带工具的流式调用 ========================

    /**
     * 流式调用 LLM API,支持 Function Calling 工具
     *
     * Phase 4.3: 发送 tools 参数，解析 tool_calls 增量
     *
     * @param context   Agent上下文
     * @param messages  消息数组
     * @param tools     tools 定义数组 (ToolRegistry.buildToolsJson()),可为null
     * @param emitter   SSE发射器
     * @param callback  流式回调(内容增量 + 完成通知含tool_calls)
     */
    public void chatStreamWithTools(AgentContext context, JSONArray messages, JSONArray tools,
                                    SseEmitter emitter, StreamCallback callback) {
        try {
            String baseUrl = context.getBaseUrl();
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            String apiUrl = baseUrl + "chat/completions";

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", context.getModelName() != null ? context.getModelName() : "gpt-3.5-turbo");
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            requestBody.put("temperature", context.getTemperature());
            requestBody.put("max_tokens", context.getMaxTokens());
            requestBody.put("stream_options", Map.of("include_usage", true));

            // Phase 4.3: 发送 tools 参数
            if (tools != null && !tools.isEmpty()) {
                requestBody.put("tools", tools);
            }

            // 使用 HttpURLConnection 进行流式读取
            URI uri = URI.create(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + context.getApiKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000);

            // 发送请求体
            byte[] body = JSON.toJSONString(requestBody).getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            // 读取流式响应
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMsg = readErrorResponse(connection);
                emitter.send(SseEmitter.event().name("error").data("LLM API错误: " + responseCode + " " + errorMsg));
                emitter.complete();
                return;
            }

            // 流式解析: 同时累积 content 和 tool_calls
            StringBuilder fullContent = new StringBuilder();
            Map<Integer, ToolCallAccumulator> toolCallAccum = new ConcurrentHashMap<>();
            boolean isToolCall = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JSONObject json = JSON.parseObject(data);
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject delta = choice.getJSONObject("delta");
                                if (delta == null) continue;

                                // 1) 检测 tool_calls
                                JSONArray deltaToolCalls = delta.getJSONArray("tool_calls");
                                if (deltaToolCalls != null && !deltaToolCalls.isEmpty()) {
                                    isToolCall = true;
                                    if (toolCallAccum.isEmpty()) {
                                        callback.onToolCallStart();
                                    }
                                    parseToolCallDelta(deltaToolCalls, toolCallAccum);
                                    continue;
                                }

                                // 2) 解析 content (始终累积,某些模型在 tool_calls 之后还会输出文本)
                                if (delta.containsKey("content")) {
                                    String chunk = delta.getString("content");
                                    if (chunk != null && !chunk.isEmpty()) {
                                        fullContent.append(chunk);
                                        // 仅在无 tool_call 时作为流式输出
                                        if (!isToolCall) {
                                            callback.onContent(chunk);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 跳过解析失败的行
                        }
                    }
                }
            }

            // 构建 ToolCall 列表
            List<ToolCall> toolCalls = Collections.emptyList();
            if (isToolCall && !toolCallAccum.isEmpty()) {
                toolCalls = new ArrayList<>(toolCallAccum.size());
                for (ToolCallAccumulator accum : toolCallAccum.values()) {
                    toolCalls.add(accum.toToolCall());
                }
            }

            // 完成回调
            callback.onDone(fullContent.toString(), toolCalls);

        } catch (IOException e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("流式请求失败: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    // ======================== tool_calls 增量解析 ========================

    /**
     * 工具调用增量累加器 — 处理流式 tool_calls 拆块发送
     */
    private static class ToolCallAccumulator {
        String id;
        String type;
        String functionName;
        StringBuilder argumentsBuilder = new StringBuilder();

        ToolCall toToolCall() {
            return new ToolCall(id, type, functionName, argumentsBuilder.toString());
        }
    }

    /**
     * 解析单行 SSE 数据中的 tool_calls delta
     */
    private void parseToolCallDelta(JSONArray deltaToolCalls, Map<Integer, ToolCallAccumulator> accum) {
        for (int i = 0; i < deltaToolCalls.size(); i++) {
            JSONObject tcDelta = deltaToolCalls.getJSONObject(i);
            int index = tcDelta.getIntValue("index", 0);

            ToolCallAccumulator tca = accum.computeIfAbsent(index, k -> new ToolCallAccumulator());

            // 第一个chunk: 包含 id, type, function.name
            if (tcDelta.containsKey("id")) {
                tca.id = tcDelta.getString("id");
            }
            if (tcDelta.containsKey("type")) {
                tca.type = tcDelta.getString("type");
            }

            JSONObject function = tcDelta.getJSONObject("function");
            if (function != null) {
                if (function.containsKey("name")) {
                    String name = function.getString("name");
                    // 防止后续 chunk 用空字符串覆盖已有的函数名
                    if (name != null && !name.isEmpty()) {
                        tca.functionName = name;
                    }
                }
                if (function.containsKey("arguments")) {
                    tca.argumentsBuilder.append(function.getString("arguments"));
                }
            }
        }
    }

    // ======================== 同步方法(保留) ========================

    /**
     * 非流式调用LLM API
     */
    public String chatSync(AgentContext context, JSONArray messages) {
        String baseUrl = context.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String apiUrl = baseUrl + "chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", context.getModelName() != null ? context.getModelName() : "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        requestBody.put("temperature", context.getTemperature());
        requestBody.put("max_tokens", context.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + context.getApiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        String body = response.getBody();
        if (body != null) {
            JSONObject jsonObject = JSON.parseObject(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }
        }
        return null;
    }

    /**
     * 调用LLM生成摘要
     */
    public String generateSummary(AgentContext context, JSONArray summaryMessages) {
        try {
            return chatSync(context, summaryMessages);
        } catch (Exception e) {
            return null;
        }
    }

    // --- private helpers ---

    private String readErrorResponse(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() > 200) {
                return sb.substring(0, 200);
            }
            return sb.toString();
        } catch (Exception e) {
            return "unknown error";
        }
    }
}
