package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiTool;
import com.ruoyi.system.domain.vo.ToolRegistryStatusVO;
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
     * 批量变更工具启用/停用状态 (Phase 4.5)
     * 内置工具可变更状态，但不可删除
     */
    @Override
    public int changeStatus(Long[] ids, String status) {
        for (Long id : ids) {
            AiTool tool = aiToolMapper.selectAiToolById(id);
            if (tool == null) {
                throw new RuntimeException("工具ID " + id + " 不存在");
            }
        }
        return aiToolMapper.updateStatusByIds(ids, status, SecurityUtils.getUsername());
    }

    /**
     * 查询工具注册状态 (Phase 4.5 前后端联动)
     *
     * 模拟 ToolRegistry 加载逻辑: 对每个启用工具尝试 Class.forName,
     * 判断处理器类是否在 classpath 中可用。
     */
    @Override
    public Map<String, Object> getRegistryStatus() {
        // 加载全部工具(含启用的和停用的)
        List<AiTool> allTools = aiToolMapper.selectAiToolList(new AiTool());
        List<ToolRegistryStatusVO> vos = new ArrayList<>();
        int registeredCount = 0;
        int failedCount = 0;

        for (AiTool tool : allTools) {
            String registryStatus;
            String errorMsg = null;

            if ("1".equals(tool.getStatus())) {
                // DB中已停用
                registryStatus = "DISABLED";
            } else if (StringUtils.isEmpty(tool.getHandlerClass())) {
                registryStatus = "CLASS_MISSING";
                errorMsg = "未配置处理器类";
                failedCount++;
            } else {
                try {
                    Class.forName(tool.getHandlerClass());
                    registryStatus = "REGISTERED";
                    registeredCount++;
                } catch (ClassNotFoundException e) {
                    registryStatus = "CLASS_MISSING";
                    errorMsg = "类未找到: " + tool.getHandlerClass();
                    failedCount++;
                } catch (Exception e) {
                    registryStatus = "CLASS_MISSING";
                    errorMsg = "类加载异常: " + e.getMessage();
                    failedCount++;
                }
            }

            vos.add(new ToolRegistryStatusVO(
                    tool.getToolId(), tool.getToolCode(), tool.getToolName(),
                    tool.getHandlerClass(), tool.getIsBuiltin(), tool.getStatus(),
                    registryStatus, errorMsg
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalEnabled", allTools.stream().filter(t -> "0".equals(t.getStatus())).count());
        result.put("registeredCount", registeredCount);
        result.put("failedCount", failedCount);
        result.put("tools", vos);
        return result;
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
