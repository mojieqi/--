package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiDocumentChunk;

/**
 * Embedding向量化Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IEmbeddingService {

    /**
     * 对文本进行向量化
     *
     * @param text 文本内容
     * @return 向量数组
     */
    List<Double> embed(String text);

    /**
     * 对单个分块进行向量化并更新数据库
     */
    void embedChunk(Long chunkId);

    /**
     * 对知识库的全部未向量化分块进行向量化
     */
    void embedKnowledgeBase(Long kbId);

    /**
     * 在知识库中进行语义搜索
     *
     * @param kbId     知识库ID
     * @param query    查询文本
     * @param topK     返回Top-K结果
     * @return 相关度排序的分块列表 + 相似度评分
     */
    List<ChunkSearchResult> semanticSearch(Long kbId, String query, int topK);

    /**
     * 搜索单个知识库
     */
    List<ChunkSearchResult> searchInBase(Long kbId, String query, int topK);

    /**
     * 计算结果集合并为RAG上下文文本
     */
    String buildKnowledgeContext(List<ChunkSearchResult> results, int maxChars);

    /**
     * 搜索结果DTO
     */
    class ChunkSearchResult {
        private Long chunkId;
        private Long docId;
        private Long kbId;
        private String chunkContent;
        private Double similarityScore;

        public ChunkSearchResult() {}
        public ChunkSearchResult(Long chunkId, Long docId, Long kbId, String chunkContent, Double similarityScore) {
            this.chunkId = chunkId;
            this.docId = docId;
            this.kbId = kbId;
            this.chunkContent = chunkContent;
            this.similarityScore = similarityScore;
        }

        public Long getChunkId() { return chunkId; }
        public void setChunkId(Long chunkId) { this.chunkId = chunkId; }
        public Long getDocId() { return docId; }
        public void setDocId(Long docId) { this.docId = docId; }
        public Long getKbId() { return kbId; }
        public void setKbId(Long kbId) { this.kbId = kbId; }
        public String getChunkContent() { return chunkContent; }
        public void setChunkContent(String chunkContent) { this.chunkContent = chunkContent; }
        public Double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    }
}
