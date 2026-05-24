package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiConversation;
import java.util.List;

/**
 * 对话会话Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiConversationService {

    AiConversation selectAiConversationById(Long conversationId);

    List<AiConversation> selectAiConversationList(AiConversation conversation);

    int insertAiConversation(AiConversation conversation);

    int updateAiConversation(AiConversation conversation);

    int deleteAiConversationById(Long conversationId);

    int deleteAiConversationByIds(Long[] conversationIds);

    /** 清空会话消息(重置上下文) */
    int clearConversation(Long conversationId);
}
