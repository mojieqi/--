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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LLM调用器 — 负责调用LLM API的流式请求
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class LlmCaller {

    private final RestTemplate restTemplate;

    public LlmCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 流式调用LLM API，通过SSE返回
     *
     * @param context   Agent上下文
     * @param messages  消息数组
     * @param emitter   SseEmitter
     * @param onComplete 完成回调(接收完整回复内容)
     */
    public void chatStream(AgentContext context, JSONArray messages, SseEmitter emitter,
                           Consumer<String> onComplete) {
        try {
            String baseUrl = context.getBaseUrl();
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            String apiUrl = baseUrl + "chat/completions";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", context.getModelName() != null ? context.getModelName() : "gpt-3.5-turbo");
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            requestBody.put("temperature", context.getTemperature());
            requestBody.put("max_tokens", context.getMaxTokens());
            requestBody.put("stream_options", Map.of("include_usage", true));

            // 使用 HttpURLConnection 进行流式读取
            URI uri = URI.create(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + context.getApiKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000); // 5分钟读超时

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

            StringBuilder fullContent = new StringBuilder();
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
                            String chunk = parseStreamChunk(data);
                            if (chunk != null && !chunk.isEmpty()) {
                                fullContent.append(chunk);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            }
                        } catch (Exception e) {
                            // 跳过解析失败的行
                        }
                    }
                }
            }

            // 完成
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();

            if (onComplete != null) {
                onComplete.accept(fullContent.toString());
            }

        } catch (IOException e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("流式请求失败: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

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

    private String parseStreamChunk(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject delta = choice.getJSONObject("delta");
                if (delta != null && delta.containsKey("content")) {
                    return delta.getString("content");
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }

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
