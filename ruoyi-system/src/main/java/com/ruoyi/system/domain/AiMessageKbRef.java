package com.ruoyi.system.domain;

/**
 * 消息知识库引用对象 ai_message_kb_ref
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiMessageKbRef {

    private static final long serialVersionUID = 1L;

    /** 引用ID */
    private Long refId;

    /** 消息ID */
    private Long messageId;

    /** 分块ID */
    private Long chunkId;

    /** 相关度评分 */
    private Double relevanceScore;

    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getChunkId() { return chunkId; }
    public void setChunkId(Long chunkId) { this.chunkId = chunkId; }
    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    @Override
    public String toString() {
        return "AiMessageKbRef{" +
                "refId=" + refId +
                ", messageId=" + messageId +
                ", chunkId=" + chunkId +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
}
