package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AiConversation;
import com.ruoyi.system.mapper.AiConversationMapper;
import com.ruoyi.system.mapper.AiConversationMessageMapper;
import com.ruoyi.system.service.IAiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话会话Service实现
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiConversationServiceImpl implements IAiConversationService {

    @Autowired
    private AiConversationMapper conversationMapper;

    @Autowired
    private AiConversationMessageMapper messageMapper;

    @Override
    public AiConversation selectAiConversationById(Long conversationId) {
        return conversationMapper.selectAiConversationById(conversationId);
    }

    @Override
    public List<AiConversation> selectAiConversationList(AiConversation conversation) {
        return conversationMapper.selectAiConversationList(conversation);
    }

    @Override
    public int insertAiConversation(AiConversation conversation) {
        if (conversation.getTitle() == null || conversation.getTitle().isEmpty()) {
            conversation.setTitle("新对话");
        }
        if (conversation.getMemoryMode() == null) {
            conversation.setMemoryMode("1");
        }
        if (conversation.getMaxTokens() == null) {
            conversation.setMaxTokens(4096);
        }
        if (conversation.getStatus() == null) {
            conversation.setStatus("0");
        }
        return conversationMapper.insertAiConversation(conversation);
    }

    @Override
    public int updateAiConversation(AiConversation conversation) {
        return conversationMapper.updateAiConversation(conversation);
    }

    @Override
    @Transactional
    public int deleteAiConversationById(Long conversationId) {
        messageMapper.deleteAiConversationMessageByConversationId(conversationId);
        return conversationMapper.deleteAiConversationById(conversationId);
    }

    @Override
    @Transactional
    public int deleteAiConversationByIds(Long[] conversationIds) {
        for (Long id : conversationIds) {
            messageMapper.deleteAiConversationMessageByConversationId(id);
        }
        return conversationMapper.deleteAiConversationByIds(conversationIds);
    }

    @Override
    @Transactional
    public int clearConversation(Long conversationId) {
        messageMapper.deleteAiConversationMessageByConversationId(conversationId);
        conversationMapper.updateMessageCount(conversationId);
        return 1;
    }
}
