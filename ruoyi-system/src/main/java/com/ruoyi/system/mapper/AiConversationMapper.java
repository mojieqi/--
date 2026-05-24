package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiConversation;
import java.util.List;

/**
 * AI对话会话Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiConversationMapper {

    AiConversation selectAiConversationById(Long conversationId);

    List<AiConversation> selectAiConversationList(AiConversation conversation);

    int insertAiConversation(AiConversation conversation);

    int updateAiConversation(AiConversation conversation);

    int deleteAiConversationById(Long conversationId);

    int deleteAiConversationByIds(Long[] conversationIds);

    int updateMessageCount(Long conversationId);
}
