package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI知识库文档对象 ai_knowledge_document
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public class AiKnowledgeDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long docId;

    /** 知识库ID */
    private Long kbId;

    /** 文档名称 */
    private String docName;

    /** 文档类型(txt/pdf/docx/xlsx/md) */
    private String docType;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 提取的文本内容 */
    private String contentText;

    /** 分块数量 */
    private Integer chunkCount;

    /** 状态(0处理中 1完成 2失败) */
    private String status;

    /** 错误信息 */
    private String errorMsg;

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

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "AiKnowledgeDocument{" +
                "docId=" + docId +
                ", kbId=" + kbId +
                ", docName='" + docName + '\'' +
                ", docType='" + docType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
