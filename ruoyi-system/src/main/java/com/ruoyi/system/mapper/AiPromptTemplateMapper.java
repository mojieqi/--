package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AiPromptTemplate;
import com.ruoyi.system.domain.vo.AiPromptTemplateQuery;

/**
 * AI提示词模板Mapper接口
 *
 * @author ruoyi
 */
public interface AiPromptTemplateMapper {

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
     * 根据场景代码查询启用的模板
     *
     * @param sceneCode 场景代码
     * @return 提示词模板集合
     */
    public List<AiPromptTemplate> selectBySceneCode(String sceneCode);

    /**
     * 查询所有启用的系统提示词(用于下拉选择)
     *
     * @return 系统提示词集合
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
}
