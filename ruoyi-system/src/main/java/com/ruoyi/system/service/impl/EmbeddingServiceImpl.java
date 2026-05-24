package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiDocumentChunk;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.mapper.AiDocumentChunkMapper;
import com.ruoyi.system.mapper.AiLlmConfigMapper;
import com.ruoyi.system.service.IEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Embedding向量化Service实现
 *
 * 使用LLM Embedding API进行文本向量化，
 * 通过余弦相似度进行语义搜索。
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class EmbeddingServiceImpl implements IEmbeddingService {

    @Autowired
    private AiDocumentChunkMapper chunkMapper;

    @Autowired
    private AiLlmConfigMapper llmConfigMapper;

    @Autowired
    private RestTemplate restTemplate;

    /** 默认向量维度 */
    private static final int DEFAULT_DIMENSION = 1536;

    /** 默认 Embedding API 路径后缀 */
    private static final String EMBEDDING_PATH = "/embeddings";

    @Override
    public List<Double> embed(String text) {
        AiLlmConfig config = getAvailableLlmConfig();
        if (config == null) {
            throw new RuntimeException("没有可用的LLM配置用于向量化");
        }

        String baseUrl = config.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String apiUrl = baseUrl.replaceAll("/chat/completions$", "") + EMBEDDING_PATH;
        if (!apiUrl.endsWith(EMBEDDING_PATH)) {
            apiUrl = baseUrl + "embeddings";
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", "text-embedding-ada-002");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getApiKey());
        headers.set("Content-Type", "application/json");

        try {
            HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String body = response.getBody();

            if (body != null) {
                JSONObject json = JSON.parseObject(body);
                JSONArray data = json.getJSONArray("data");
                if (data != null && !data.isEmpty()) {
                    JSONObject first = data.getJSONObject(0);
                    JSONArray embedding = first.getJSONArray("embedding");
                    if (embedding != null) {
                        List<Double> vector = new ArrayList<>();
                        for (int i = 0; i < embedding.size(); i++) {
                            vector.add(embedding.getDouble(i));
                        }
                        return vector;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("向量化失败: " + e.getMessage());
        }

        throw new RuntimeException("向量化返回为空");
    }

    @Override
    public void embedChunk(Long chunkId) {
        AiDocumentChunk chunk = chunkMapper.selectAiDocumentChunkById(chunkId);
        if (chunk == null) return;

        List<Double> vector = embed(chunk.getChunkContent());
        chunkMapper.updateChunkVector(chunkId, JSON.toJSONString(vector));
    }

    @Override
    public void embedKnowledgeBase(Long kbId) {
        List<AiDocumentChunk> chunks = chunkMapper.selectChunksByKbId(kbId);
        for (AiDocumentChunk chunk : chunks) {
            if (chunk.getChunkVector() == null || chunk.getChunkVector().isEmpty()) {
                try {
                    embedChunk(chunk.getChunkId());
                    Thread.sleep(200); // 避免API限流
                } catch (Exception e) {
                    // 跳过失败的分块，继续处理
                }
            }
        }
    }

    @Override
    public List<ChunkSearchResult> semanticSearch(Long kbId, String query, int topK) {
        return searchInBase(kbId, query, topK);
    }

    @Override
    public List<ChunkSearchResult> searchInBase(Long kbId, String query, int topK) {
        // 1. 向量化查询文本
        List<Double> queryVector;
        try {
            queryVector = embed(query);
        } catch (Exception e) {
            // 向量化失败时降级为关键词匹配
            return fallbackKeywordSearch(kbId, query, topK);
        }

        // 2. 获取知识库所有已向量化的分块
        List<AiDocumentChunk> chunks = chunkMapper.selectChunksByKbId(kbId);

        // 3. 计算相似度并排序
        List<ChunkSearchResult> results = new ArrayList<>();
        for (AiDocumentChunk chunk : chunks) {
            if (chunk.getChunkVector() == null || chunk.getChunkVector().isEmpty()) {
                continue;
            }
            try {
                List<Double> chunkVector = parseVector(chunk.getChunkVector());
                double similarity = cosineSimilarity(queryVector, chunkVector);
                results.add(new ChunkSearchResult(
                        chunk.getChunkId(), chunk.getDocId(), chunk.getKbId(),
                        chunk.getChunkContent(), similarity
                ));
            } catch (Exception e) {
                continue;
            }
        }

        // 4. 按相似度降序排序，取TopK
        results.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        return results.subList(0, Math.min(topK, results.size()));
    }

    @Override
    public String buildKnowledgeContext(List<ChunkSearchResult> results, int maxChars) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int totalChars = 0;
        int index = 1;

        for (ChunkSearchResult result : results) {
            String content = result.getChunkContent();
            if (content == null || content.isEmpty()) continue;

            if (totalChars + content.length() > maxChars) {
                int remaining = maxChars - totalChars;
                if (remaining > 50) {
                    sb.append("【参考").append(index).append("】(相关度: ")
                            .append(String.format("%.2f", result.getSimilarityScore()))
                            .append(")\n").append(content, 0, remaining).append("\n\n");
                }
                break;
            }

            sb.append("【参考").append(index).append("】(相关度: ")
                    .append(String.format("%.2f", result.getSimilarityScore()))
                    .append(")\n").append(content).append("\n\n");
            totalChars += content.length() + 30;
            index++;
        }

        return sb.toString();
    }

    /**
     * 计算两个向量的余弦相似度
     */
    public static double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量维度不一致: " + vec1.size() + " vs " + vec2.size());
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // --- private helpers ---

    private AiLlmConfig getAvailableLlmConfig() {
        com.ruoyi.system.domain.vo.AiLlmConfigQuery query =
                new com.ruoyi.system.domain.vo.AiLlmConfigQuery();
        query.setStatus("0");
        List<AiLlmConfig> configs = llmConfigMapper.selectAiLlmConfigList(query);
        for (AiLlmConfig config : configs) {
            if ("1".equals(config.getIsDefault()) && StringUtils.isNotEmpty(config.getApiKey())) {
                return config;
            }
        }
        for (AiLlmConfig config : configs) {
            if (StringUtils.isNotEmpty(config.getApiKey())) {
                return config;
            }
        }
        return null;
    }

    private List<Double> parseVector(String vectorJson) {
        JSONArray arr = JSON.parseArray(vectorJson);
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            result.add(arr.getDouble(i));
        }
        return result;
    }

    private List<ChunkSearchResult> fallbackKeywordSearch(Long kbId, String query, int topK) {
        AiDocumentChunk searchParam = new AiDocumentChunk();
        searchParam.setKbId(kbId);
        searchParam.setChunkContent(query);
        List<AiDocumentChunk> chunks = chunkMapper.searchChunks(searchParam);

        return chunks.stream()
                .map(chunk -> new ChunkSearchResult(
                        chunk.getChunkId(), chunk.getDocId(), chunk.getKbId(),
                        chunk.getChunkContent(), 0.5
                ))
                .limit(topK)
                .collect(Collectors.toList());
    }
}
