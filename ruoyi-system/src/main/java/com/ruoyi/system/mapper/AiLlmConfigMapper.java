package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.domain.vo.AiLlmConfigQuery;
import org.apache.ibatis.annotations.Param;

/**
 * LLM大模型配置Mapper接口
 *
 * @author ruoyi
 */
public interface AiLlmConfigMapper {

    /**
     * 查询LLM大模型配置
     *
     * @param configId LLM大模型配置主键
     * @return LLM大模型配置
     */
    public AiLlmConfig selectAiLlmConfigById(Long configId);

    /**
     * 查询LLM大模型配置列表
     *
     * @param query 查询条件
     * @return LLM大模型配置集合
     */
    public List<AiLlmConfig> selectAiLlmConfigList(AiLlmConfigQuery query);

    /**
     * 新增LLM大模型配置
     *
     * @param config LLM大模型配置
     * @return 结果
     */
    public int insertAiLlmConfig(AiLlmConfig config);

    /**
     * 修改LLM大模型配置
     *
     * @param config LLM大模型配置
     * @return 结果
     */
    public int updateAiLlmConfig(AiLlmConfig config);

    /**
     * 删除LLM大模型配置
     *
     * @param configId LLM大模型配置主键
     * @return 结果
     */
    public int deleteAiLlmConfigById(Long configId);

    /**
     * 批量删除LLM大模型配置
     *
     * @param configIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAiLlmConfigByIds(Long[] configIds);

    /**
     * 将所有配置设为非默认
     */
    public int clearDefault();

    /**
     * 根据服务商代码获取默认配置
     *
     * @param provider 服务商代码
     * @return LLM大模型配置
     */
    public AiLlmConfig selectDefaultByProvider(@Param("provider") String provider);
}
