package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.AiTool;

/**
 * AI工具注册表Mapper接口
 *
 * @author ruoyi
 */
public interface AiToolMapper {

    /**
     * 查询AI工具
     *
     * @param toolId AI工具主键
     * @return AI工具
     */
    public AiTool selectAiToolById(Long toolId);

    /**
     * 按工具代码查询
     *
     * @param toolCode 工具代码
     * @return AI工具
     */
    public AiTool selectAiToolByCode(String toolCode);

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
     * 批量变更工具状态 (Phase 4.5 前端工具管理)
     *
     * @param ids 工具ID数组
     * @param status 目标状态(0启用 1停用)
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateStatusByIds(@Param("ids") Long[] ids,
                                  @Param("status") String status,
                                  @Param("updateBy") String updateBy);
}
