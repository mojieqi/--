package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiPromptTemplate;
import com.ruoyi.system.domain.vo.AiPromptTemplateQuery;

/**
 * AI提示词模板Service接口
 *
 * @author ruoyi
 */
public interface IAiPromptTemplateService {

    /**
     * 查询提示词模板
     *
     * @param templateId 模板主键
     * @return 提示词模板
     */
    public AiPromptTemplate selectAiPromptTemplateById(Long templateId);

    /**
     * 查询提示词模板列表
     *
     * @param query 查询条件
     * @return 提示词模板集合
     */
    public List<AiPromptTemplate> selectAiPromptTemplateList(AiPromptTemplateQuery query);

    /**
     * 查询启用的系统提示词(用于下拉选择)
     */
    public List<AiPromptTemplate> selectActiveSystemPrompts();

    /**
     * 新增提示词模板
     *
     * @param template 提示词模板
     * @return 结果
     */
    public int insertAiPromptTemplate(AiPromptTemplate template);

    /**
     * 修改提示词模板
     *
     * @param template 提示词模板
     * @return 结果
     */
    public int updateAiPromptTemplate(AiPromptTemplate template);

    /**
     * 删除提示词模板
     *
     * @param templateId 模板主键
     * @return 结果
     */
    public int deleteAiPromptTemplateById(Long templateId);

    /**
     * 批量删除提示词模板
     *
     * @param templateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAiPromptTemplateByIds(Long[] templateIds);

    /**
     * AI生成/润色提示词
     *
     * @param userInput   用户输入的需求描述
     * @param templateType 提示词类型(0系统/1用户)
     * @param mode        模式(generate生成/polish润色)
     * @return 生成的提示词
     */
    public String generatePrompt(String userInput, String templateType, String mode);
}
