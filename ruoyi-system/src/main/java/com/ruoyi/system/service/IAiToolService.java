package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiTool;

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
}
