package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI知识库对象 ai_knowledge_base
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiKnowledgeBase extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 知识库ID */
    private Long kbId;

    /** 知识库名称 */
    private String kbName;

    /** 知识库描述 */
    private String kbDesc;

    /** 向量化模型 */
    private String embeddingModel;

    /** 文档数量 */
    private Integer docCount;

    /** 分块总数 */
    private Integer chunkCount;

    /** 状态(0正常 1停用) */
    private String status;

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getKbName() {
        return kbName;
    }

    public void setKbName(String kbName) {
        this.kbName = kbName;
    }

    public String getKbDesc() {
        return kbDesc;
    }

    public void setKbDesc(String kbDesc) {
        this.kbDesc = kbDesc;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getDocCount() {
        return docCount;
    }

    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AiKnowledgeBase{" +
                "kbId=" + kbId +
                ", kbName='" + kbName + '\'' +
                ", kbDesc='" + kbDesc + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
