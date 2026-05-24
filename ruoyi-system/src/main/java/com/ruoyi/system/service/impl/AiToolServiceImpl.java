package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiTool;
import com.ruoyi.system.mapper.AiToolMapper;
import com.ruoyi.system.service.IAiToolService;

/**
 * AI工具注册表Service实现
 *
 * @author ruoyi
 */
@Service
public class AiToolServiceImpl implements IAiToolService {

    @Autowired
    private AiToolMapper aiToolMapper;

    /**
     * 查询AI工具
     */
    @Override
    public AiTool selectAiToolById(Long toolId) {
        return aiToolMapper.selectAiToolById(toolId);
    }

    /**
     * 查询AI工具列表
     */
    @Override
    public List<AiTool> selectAiToolList(AiTool tool) {
        return aiToolMapper.selectAiToolList(tool);
    }

    /**
     * 查询所有启用的工具
     */
    @Override
    public List<AiTool> selectEnabledTools() {
        return aiToolMapper.selectEnabledTools();
    }

    /**
     * 新增AI工具
     */
    @Override
    public int insertAiTool(AiTool tool) {
        validateToolCode(tool);
        return aiToolMapper.insertAiTool(tool);
    }

    /**
     * 修改AI工具
     */
    @Override
    public int updateAiTool(AiTool tool) {
        // 内置工具不可修改工具代码
        AiTool existing = aiToolMapper.selectAiToolById(tool.getToolId());
        if (existing != null && "1".equals(existing.getIsBuiltin())) {
            if (tool.getToolCode() != null && !tool.getToolCode().equals(existing.getToolCode())) {
                throw new RuntimeException("内置工具不可修改工具代码");
            }
        }
        return aiToolMapper.updateAiTool(tool);
    }

    /**
     * 删除AI工具
     */
    @Override
    public int deleteAiToolById(Long toolId) {
        // 内置工具不可删除
        AiTool existing = aiToolMapper.selectAiToolById(toolId);
        if (existing != null && "1".equals(existing.getIsBuiltin())) {
            throw new RuntimeException("内置工具不可删除");
        }
        return aiToolMapper.deleteAiToolById(toolId);
    }

    /**
     * 批量删除AI工具
     */
    @Override
    public int deleteAiToolByIds(Long[] toolIds) {
        int count = 0;
        for (Long toolId : toolIds) {
            count += deleteAiToolById(toolId);
        }
        return count;
    }

    /**
     * 校验工具代码唯一性
     */
    private void validateToolCode(AiTool tool) {
        if (StringUtils.isEmpty(tool.getToolCode())) {
            return;
        }
        AiTool existing = aiToolMapper.selectAiToolByCode(tool.getToolCode());
        if (existing != null && !existing.getToolId().equals(tool.getToolId())) {
            throw new RuntimeException("工具代码 '" + tool.getToolCode() + "' 已存在");
        }
    }
}
