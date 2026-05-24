package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiConversationMessage;
import java.util.List;

/**
 * AI对话消息Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiConversationMessageMapper {

    AiConversationMessage selectAiConversationMessageById(Long messageId);

    List<AiConversationMessage> selectAiConversationMessageList(AiConversationMessage message);

    int insertAiConversationMessage(AiConversationMessage message);

    int deleteAiConversationMessageByConversationId(Long conversationId);

    int deleteAiConversationMessageByIds(Long[] messageIds);

    /** 获取会话最近N条消息 */
    List<AiConversationMessage> selectRecentMessages(Long conversationId, Integer limit);

    /** 获取会话消息总数 */
    int selectMessageCount(Long conversationId);
}
