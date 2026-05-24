package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiKnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * AI知识库文档Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiKnowledgeDocumentService {

    /**
     * 查询文档
     */
    AiKnowledgeDocument selectAiKnowledgeDocumentById(Long docId);

    /**
     * 查询文档列表
     */
    List<AiKnowledgeDocument> selectAiKnowledgeDocumentList(AiKnowledgeDocument document);

    /**
     * 上传并解析文档
     */
    AiKnowledgeDocument uploadDocument(MultipartFile file, Long kbId) throws Exception;

    /**
     * 重新解析文档
     */
    AiKnowledgeDocument reparseDocument(Long docId) throws Exception;

    /**
     * 删除文档(级联删除分块)
     */
    int deleteAiKnowledgeDocumentByIds(Long[] docIds);
}
