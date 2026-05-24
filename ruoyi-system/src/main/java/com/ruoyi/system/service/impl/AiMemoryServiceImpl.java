package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AiUserMemory;
import com.ruoyi.system.mapper.AiUserMemoryMapper;
import com.ruoyi.system.service.IAiMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 记忆管理Service实现
 *
 * Phase 3: 仅提供基础CRUD
 * Phase 4: 将实现自动记忆提取和注入
 *
 * @author ruoyi
 * @date 2026-05-23
 */
@Service
public class AiMemoryServiceImpl implements IAiMemoryService {

    @Autowired
    private AiUserMemoryMapper memoryMapper;

    @Override
    public List<AiUserMemory> selectAiUserMemoryList(AiUserMemory memory) {
        return memoryMapper.selectAiUserMemoryList(memory);
    }

    @Override
    public List<AiUserMemory> selectAiUserMemoryByUserId(Long userId) {
        return memoryMapper.selectAiUserMemoryByUserId(userId);
    }

    @Override
    public int insertAiUserMemory(AiUserMemory memory) {
        return memoryMapper.insertAiUserMemory(memory);
    }

    @Override
    public int updateAiUserMemory(AiUserMemory memory) {
        return memoryMapper.updateAiUserMemory(memory);
    }

    @Override
    public int deleteAiUserMemoryByIds(Long[] memoryIds) {
        return memoryMapper.deleteAiUserMemoryByIds(memoryIds);
    }
}
