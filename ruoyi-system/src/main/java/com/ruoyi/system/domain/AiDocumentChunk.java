package com.ruoyi.system.domain;

/**
 * 文档分块对象 ai_document_chunk
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiDocumentChunk {

    private static final long serialVersionUID = 1L;

    /** 分块ID */
    private Long chunkId;

    /** 文档ID */
    private Long docId;

    /** 知识库ID */
    private Long kbId;

    /** 分块序号 */
    private Integer chunkIndex;

    /** 分块文本 */
    private String chunkContent;

    /** 向量数据(JSON格式) */
    private String chunkVector;

    /** Token数量 */
    private Integer tokenCount;

    public Long getChunkId() {
        return chunkId;
    }

    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChunkContent() {
        return chunkContent;
    }

    public void setChunkContent(String chunkContent) {
        this.chunkContent = chunkContent;
    }

    public String getChunkVector() {
        return chunkVector;
    }

    public void setChunkVector(String chunkVector) {
        this.chunkVector = chunkVector;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    @Override
    public String toString() {
        return "AiDocumentChunk{" +
                "chunkId=" + chunkId +
                ", docId=" + docId +
                ", kbId=" + kbId +
                ", chunkIndex=" + chunkIndex +
                '}';
    }
}
