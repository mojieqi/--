package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiKnowledgeDocument;
import java.util.List;

/**
 * AI知识库文档Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiKnowledgeDocumentMapper {

    /**
     * 查询文档
     */
    AiKnowledgeDocument selectAiKnowledgeDocumentById(Long docId);

    /**
     * 查询文档列表
     */
    List<AiKnowledgeDocument> selectAiKnowledgeDocumentList(AiKnowledgeDocument document);

    /**
     * 新增文档
     */
    int insertAiKnowledgeDocument(AiKnowledgeDocument document);

    /**
     * 修改文档
     */
    int updateAiKnowledgeDocument(AiKnowledgeDocument document);

    /**
     * 删除文档
     */
    int deleteAiKnowledgeDocumentById(Long docId);

    /**
     * 批量删除文档
     */
    int deleteAiKnowledgeDocumentByIds(Long[] docIds);

    /**
     * 根据知识库ID删除全部文档
     */
    int deleteAiKnowledgeDocumentByKbId(Long kbId);
}
