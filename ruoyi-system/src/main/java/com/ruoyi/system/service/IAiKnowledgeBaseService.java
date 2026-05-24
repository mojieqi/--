package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiKnowledgeBase;
import java.util.List;

/**
 * AI知识库Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiKnowledgeBaseService {

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
     * 删除知识库(级联删除文档和分块)
     */
    int deleteAiKnowledgeBaseByIds(Long[] kbIds);

    /**
     * 更新知识库文档统计
     */
    void updateDocCount(Long kbId);
}
