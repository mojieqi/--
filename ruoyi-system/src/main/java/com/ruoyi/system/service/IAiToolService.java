package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.AiTool;
import com.ruoyi.system.domain.vo.ToolRegistryStatusVO;

/**
 * AI工具注册表Service接口
 *
 * @author ruoyi
 */
public interface IAiToolService {

    /**
     * 查询AI工具
     *
     * @param toolId AI工具主键
     * @return AI工具
     */
    public AiTool selectAiToolById(Long toolId);

    /**
     * 查询AI工具列表
     *
     * @param tool 查询条件
     * @return AI工具集合
     */
    public List<AiTool> selectAiToolList(AiTool tool);

    /**
     * 查询所有启用的工具 (Phase 4.2 ToolRegistry 使用)
     *
     * @return 启用的工具集合
     */
    public List<AiTool> selectEnabledTools();

    /**
     * 新增AI工具
     *
     * @param tool AI工具
     * @return 结果
     */
    public int insertAiTool(AiTool tool);

    /**
     * 修改AI工具
     *
     * @param tool AI工具
     * @return 结果
     */
    public int updateAiTool(AiTool tool);

    /**
     * 删除AI工具
     *
     * @param toolId AI工具主键
     * @return 结果
     */
    public int deleteAiToolById(Long toolId);

    /**
     * 批量删除AI工具
     *
     * @param toolIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAiToolByIds(Long[] toolIds);

    /**
     * 批量变更工具启用/停用状态 (Phase 4.5)
     *
     * @param ids 工具ID数组
     * @param status 目标状态(0启用 1停用)
     * @return 结果
     */
    public int changeStatus(Long[] ids, String status);

    /**
     * 查询工具注册状态 (Phase 4.5 前后端联动)
     *
     * 检查所有工具是否可被 ToolRegistry 加载:
     * - 对启用工具尝试 Class.forName 判断处理器类是否存在
     * - 返回每个工具的注册状态(REGISTERED/CLASS_MISSING/DISABLED)
     *
     * @return 注册状态结果: { "totalEnabled": N, "registeredCount": N, "failedCount": N, "tools": [...] }
     */
    public Map<String, Object> getRegistryStatus();
}
