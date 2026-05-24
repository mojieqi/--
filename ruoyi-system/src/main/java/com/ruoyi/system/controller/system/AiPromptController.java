package com.ruoyi.system.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AiPromptTemplate;
import com.ruoyi.system.domain.vo.AiPromptTemplateQuery;
import com.ruoyi.system.service.IAiPromptTemplateService;

/**
 * AI提示词管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/prompt")
public class AiPromptController extends BaseController {

    @Autowired
    private IAiPromptTemplateService promptTemplateService;

    /**
     * 获取提示词模板列表
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiPromptTemplateQuery query) {
        startPage();
        List<AiPromptTemplate> list = promptTemplateService.selectAiPromptTemplateList(query);
        return getDataTable(list);
    }

    /**
     * 获取系统提示词列表(下拉选择用)
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:list')")
    @GetMapping("/systemPrompts")
    public AjaxResult systemPrompts() {
        return success(promptTemplateService.selectActiveSystemPrompts());
    }

    /**
     * 获取提示词详情
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:list')")
    @GetMapping("/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId) {
        return success(promptTemplateService.selectAiPromptTemplateById(templateId));
    }

    /**
     * 新增提示词模板
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:add')")
    @Log(title = "提示词管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiPromptTemplate template) {
        template.setCreateBy(getUsername());
        template.setIsBuiltin("0");
        return toAjax(promptTemplateService.insertAiPromptTemplate(template));
    }

    /**
     * 修改提示词模板
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:edit')")
    @Log(title = "提示词管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiPromptTemplate template) {
        // 内置模板不允许修改
        AiPromptTemplate existing = promptTemplateService.selectAiPromptTemplateById(template.getTemplateId());
        if (existing != null && "1".equals(existing.getIsBuiltin())) {
            return error("内置模板不允许编辑");
        }
        template.setUpdateBy(getUsername());
        return toAjax(promptTemplateService.updateAiPromptTemplate(template));
    }

    /**
     * 删除提示词模板
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:remove')")
    @Log(title = "提示词管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        // 检查是否包含内置模板
        for (Long id : templateIds) {
            AiPromptTemplate template = promptTemplateService.selectAiPromptTemplateById(id);
            if (template != null && "1".equals(template.getIsBuiltin())) {
                return error("内置模板【" + template.getTemplateName() + "】不允许删除");
            }
        }
        return toAjax(promptTemplateService.deleteAiPromptTemplateByIds(templateIds));
    }

    /**
     * AI生成/润色提示词
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:generate')")
    @Log(title = "提示词管理", businessType = BusinessType.OTHER)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody AiPromptTemplate template) {
        try {
            String userInput = template.getPromptContent();
            String templateType = template.getTemplateType() != null ? template.getTemplateType() : "0";
            // 通过remark字段传递mode (generate/polish)
            String mode = template.getRemark();
            if (mode == null || mode.isEmpty()) {
                mode = "generate";
            }
            String result = promptTemplateService.generatePrompt(userInput, templateType, mode);
            return success(result);
        } catch (Exception e) {
            return error("生成失败: " + e.getMessage());
        }
    }
}
