package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiKnowledgeBase;
import java.util.List;

/**
 * AI知识库Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiKnowledgeBaseMapper {

    /**
     * 查询知识库
     */
    AiKnowledgeBase selectAiKnowledgeBaseById(Long kbId);

    /**
     * 查询知识库列表
     */
    List<AiKnowledgeBase> selectAiKnowledgeBaseList(AiKnowledgeBase knowledgeBase);

    /**
     * 新增知识库
     */
    int insertAiKnowledgeBase(AiKnowledgeBase knowledgeBase);

    /**
     * 修改知识库
     */
    int updateAiKnowledgeBase(AiKnowledgeBase knowledgeBase);

    /**
     * 删除知识库
     */
    int deleteAiKnowledgeBaseById(Long kbId);

    /**
     * 批量删除知识库
     */
    int deleteAiKnowledgeBaseByIds(Long[] kbIds);

    /**
     * 更新知识库文档数量
     */
    int updateDocCount(Long kbId);
}
