package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiMessageKbRef;
import java.util.List;

/**
 * 消息知识库引用Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiMessageKbRefMapper {

    List<AiMessageKbRef> selectAiMessageKbRefByMessageId(Long messageId);

    int insertAiMessageKbRef(AiMessageKbRef ref);

    int batchInsertAiMessageKbRef(List<AiMessageKbRef> refs);

    int deleteAiMessageKbRefByMessageId(Long messageId);
}
