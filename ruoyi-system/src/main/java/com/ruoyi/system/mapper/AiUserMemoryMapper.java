package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiUserMemory;
import java.util.List;

/**
 * 用户长期记忆Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface AiUserMemoryMapper {

    AiUserMemory selectAiUserMemoryById(Long memoryId);

    List<AiUserMemory> selectAiUserMemoryList(AiUserMemory memory);

    int insertAiUserMemory(AiUserMemory memory);

    int updateAiUserMemory(AiUserMemory memory);

    int deleteAiUserMemoryById(Long memoryId);

    int deleteAiUserMemoryByIds(Long[] memoryIds);

    List<AiUserMemory> selectAiUserMemoryByUserId(Long userId);
}
