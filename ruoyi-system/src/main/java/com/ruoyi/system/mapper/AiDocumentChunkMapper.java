package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiDocumentChunk;
import java.util.List;

/**
 * 文档分块Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiDocumentChunkMapper {

    /**
     * 查询分块
     */
    AiDocumentChunk selectAiDocumentChunkById(Long chunkId);

    /**
     * 查询文档分块列表
     */
    List<AiDocumentChunk> selectAiDocumentChunkByDocId(Long docId);

    /**
     * 批量新增分块
     */
    int batchInsertAiDocumentChunk(List<AiDocumentChunk> chunkList);

    /**
     * 根据文档ID删除分块
     */
    int deleteAiDocumentChunkByDocId(Long docId);

    /**
     * 根据知识库ID删除全部分块
     */
    int deleteAiDocumentChunkByKbId(Long kbId);

    /**
     * 检索分块(关键词匹配)
     */
    List<AiDocumentChunk> searchChunks(AiDocumentChunk searchParam);

    /**
     * 获取知识库下所有分块(用于向量化和语义搜索)
     */
    List<AiDocumentChunk> selectChunksByKbId(Long kbId);

    /**
     * 更新分块向量
     */
    int updateChunkVector(Long chunkId, String chunkVector);
}
