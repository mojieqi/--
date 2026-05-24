package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AiPromptGeneration;

/**
 * AI提示词生成记录Mapper接口
 *
 * @author ruoyi
 */
public interface AiPromptGenerationMapper {

    /**
     * 新增生成记录
     *
     * @param generation 生成记录
     * @return 结果
     */
    public int insertAiPromptGeneration(AiPromptGeneration generation);
}
