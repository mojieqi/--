package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AiKnowledgeBase;
import com.ruoyi.system.mapper.AiDocumentChunkMapper;
import com.ruoyi.system.mapper.AiKnowledgeBaseMapper;
import com.ruoyi.system.mapper.AiKnowledgeDocumentMapper;
import com.ruoyi.system.service.IAiKnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI知识库Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiKnowledgeBaseServiceImpl implements IAiKnowledgeBaseService {

    @Autowired
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Autowired
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Autowired
    private AiDocumentChunkMapper aiDocumentChunkMapper;

    @Override
    public AiKnowledgeBase selectAiKnowledgeBaseById(Long kbId) {
        return aiKnowledgeBaseMapper.selectAiKnowledgeBaseById(kbId);
    }

    @Override
    public List<AiKnowledgeBase> selectAiKnowledgeBaseList(AiKnowledgeBase knowledgeBase) {
        return aiKnowledgeBaseMapper.selectAiKnowledgeBaseList(knowledgeBase);
    }

    @Override
    public int insertAiKnowledgeBase(AiKnowledgeBase knowledgeBase) {
        return aiKnowledgeBaseMapper.insertAiKnowledgeBase(knowledgeBase);
    }

    @Override
    public int updateAiKnowledgeBase(AiKnowledgeBase knowledgeBase) {
        return aiKnowledgeBaseMapper.updateAiKnowledgeBase(knowledgeBase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAiKnowledgeBaseByIds(Long[] kbIds) {
        for (Long kbId : kbIds) {
            aiDocumentChunkMapper.deleteAiDocumentChunkByKbId(kbId);
            aiKnowledgeDocumentMapper.deleteAiKnowledgeDocumentByKbId(kbId);
        }
        return aiKnowledgeBaseMapper.deleteAiKnowledgeBaseByIds(kbIds);
    }

    @Override
    public void updateDocCount(Long kbId) {
        aiKnowledgeBaseMapper.updateDocCount(kbId);
    }
}
