package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.domain.vo.AiLlmConfigQuery;

/**
 * LLM大模型配置Service接口
 *
 * @author ruoyi
 */
public interface IAiLlmConfigService {

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
     * 设置默认配置
     *
     * @param configId 配置ID
     * @return 结果
     */
    public int setDefault(Long configId);

    /**
     * 测试接入 - 获取可用模型列表
     *
     * @param configId 配置ID
     * @return 模型列表
     */
    public List<String> testConnection(Long configId);
}
