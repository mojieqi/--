package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiUserMemory;
import java.util.List;

/**
 * 记忆管理Service接口
 *
 * @author ruoyi
 * @date 2026-05-23
 */
public interface IAiMemoryService {

    List<AiUserMemory> selectAiUserMemoryList(AiUserMemory memory);

    List<AiUserMemory> selectAiUserMemoryByUserId(Long userId);

    int insertAiUserMemory(AiUserMemory memory);

    int updateAiUserMemory(AiUserMemory memory);

    int deleteAiUserMemoryByIds(Long[] memoryIds);
}
