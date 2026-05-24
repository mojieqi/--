package com.ruoyi.system.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.domain.vo.AiLlmConfigQuery;
import com.ruoyi.system.service.IAiLlmConfigService;

/**
 * LLM大模型配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/llm/config")
public class AiLlmConfigController extends BaseController {

    @Autowired
    private IAiLlmConfigService aiLlmConfigService;

    /**
     * 获取配置列表
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiLlmConfigQuery query) {
        startPage();
        List<AiLlmConfig> list = aiLlmConfigService.selectAiLlmConfigList(query);
        return getDataTable(list);
    }

    /**
     * 导出配置列表
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:export')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, AiLlmConfigQuery query) {
        List<AiLlmConfig> list = aiLlmConfigService.selectAiLlmConfigList(query);
        ExcelUtil<AiLlmConfig> util = new ExcelUtil<>(AiLlmConfig.class);
        util.exportExcel(response, list, "LLM大模型配置数据");
    }

    /**
     * 获取配置详情
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:list')")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId) {
        return success(aiLlmConfigService.selectAiLlmConfigById(configId));
    }

    /**
     * 新增配置
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:add')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiLlmConfig config) {
        config.setCreateBy(getUsername());
        return toAjax(aiLlmConfigService.insertAiLlmConfig(config));
    }

    /**
     * 修改配置
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:edit')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiLlmConfig config) {
        config.setUpdateBy(getUsername());
        return toAjax(aiLlmConfigService.updateAiLlmConfig(config));
    }

    /**
     * 删除配置
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:remove')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) {
        return toAjax(aiLlmConfigService.deleteAiLlmConfigByIds(configIds));
    }

    /**
     * 设置默认配置
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:edit')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.UPDATE)
    @PutMapping("/default/{configId}")
    public AjaxResult setDefault(@PathVariable Long configId) {
        return toAjax(aiLlmConfigService.setDefault(configId));
    }

    /**
     * 测试接入 - 获取模型列表
     */
    @PreAuthorize("@ss.hasPermi('ai:llm:list')")
    @Log(title = "LLM大模型配置", businessType = BusinessType.OTHER)
    @PostMapping("/test/{configId}")
    public AjaxResult testConnection(@PathVariable Long configId) {
        try {
            List<String> models = aiLlmConfigService.testConnection(configId);
            return success(models);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}
