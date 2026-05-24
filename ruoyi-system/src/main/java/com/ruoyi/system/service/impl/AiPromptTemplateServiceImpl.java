package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiPromptTemplate;
import com.ruoyi.system.domain.AiPromptGeneration;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.domain.vo.AiPromptTemplateQuery;
import com.ruoyi.system.mapper.AiPromptTemplateMapper;
import com.ruoyi.system.mapper.AiPromptGenerationMapper;
import com.ruoyi.system.mapper.AiLlmConfigMapper;
import com.ruoyi.system.service.IAiPromptTemplateService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * AI提示词模板Service实现
 *
 * @author ruoyi
 */
@Service
public class AiPromptTemplateServiceImpl implements IAiPromptTemplateService {

    @Autowired
    private AiPromptTemplateMapper promptTemplateMapper;

    @Autowired
    private AiPromptGenerationMapper promptGenerationMapper;

    @Autowired
    private AiLlmConfigMapper llmConfigMapper;

    @Autowired
    private RestTemplate restTemplate;

    /** 用于生成提示词的元提示词 */
    private static final String PROMPT_GENERATOR_PROMPT =
        "你是一个专业的提示词工程师。以下是项目的背景信息：\n" +
        "项目：AI校园墙智能平台，依托人工智能技术打造校园一体化交流服务社区。" +
        "支持用户发布表白、寻物、求助、校园资讯、生活吐槽等动态内容；" +
        "集成AI智能内容审核、AI违规识别、智能推荐、智能分类功能。\n\n";

    /**
     * 查询提示词模板
     */
    @Override
    public AiPromptTemplate selectAiPromptTemplateById(Long templateId) {
        return promptTemplateMapper.selectAiPromptTemplateById(templateId);
    }

    /**
     * 查询提示词模板列表
     */
    @Override
    public List<AiPromptTemplate> selectAiPromptTemplateList(AiPromptTemplateQuery query) {
        return promptTemplateMapper.selectAiPromptTemplateList(query);
    }

    /**
     * 查询启用的系统提示词
     */
    @Override
    public List<AiPromptTemplate> selectActiveSystemPrompts() {
        return promptTemplateMapper.selectActiveSystemPrompts();
    }

    /**
     * 新增提示词模板
     */
    @Override
    public int insertAiPromptTemplate(AiPromptTemplate template) {
        return promptTemplateMapper.insertAiPromptTemplate(template);
    }

    /**
     * 修改提示词模板
     */
    @Override
    public int updateAiPromptTemplate(AiPromptTemplate template) {
        return promptTemplateMapper.updateAiPromptTemplate(template);
    }

    /**
     * 删除提示词模板
     */
    @Override
    public int deleteAiPromptTemplateById(Long templateId) {
        return promptTemplateMapper.deleteAiPromptTemplateById(templateId);
    }

    /**
     * 批量删除提示词模板
     */
    @Override
    public int deleteAiPromptTemplateByIds(Long[] templateIds) {
        return promptTemplateMapper.deleteAiPromptTemplateByIds(templateIds);
    }

    /**
     * AI生成/润色提示词
     */
    @Override
    public String generatePrompt(String userInput, String templateType, String mode) {
        // 1. 获取默认启用的LLM配置
        AiLlmConfig llmConfig = getDefaultLlmConfig();
        if (llmConfig == null) {
            throw new RuntimeException("没有可用的LLM配置，请先在【LLM大模型配置】中添加并设置默认配置");
        }

        // 2. 构建生成提示词的元提示词
        String typeName = "0".equals(templateType) ? "系统提示词" : "用户提示词";
        String modeDesc = "polish".equals(mode) ? "润色优化" : "生成";
        String instruction = "polish".equals(mode)
            ? "请对以下提示词进行润色优化，使其更专业、更清晰、更适合AI理解：\n\n" + userInput
            : "请根据以下需求，生成一个专业的" + typeName + "：\n\n" + userInput +
              "\n\n要求：\n" +
              "1. 提示词应当结构清晰，包含角色定位、能力范围、输出格式\n" +
              "2. 如果是系统提示词，要明确AI的角色、规则和约束\n" +
              "3. 如果是用户提示词，要包含可替换的变量占位符如{{变量名}}\n" +
              "4. 提示词要贴合校园墙场景";

        String systemPrompt = PROMPT_GENERATOR_PROMPT + "\n你需要" + modeDesc + typeName + "。\n只输出提示词内容，不要添加解释或说明。";

        // 3. 调用LLM API
        try {
            String baseUrl = llmConfig.getBaseUrl();
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            String apiUrl = baseUrl + "chat/completions";

            // 构建请求体 (OpenAI兼容格式)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmConfig.getDefaultModel() != null ? llmConfig.getDefaultModel() : "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", instruction);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + llmConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(JSON.toJSONString(requestBody), headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String body = response.getBody();

            if (StringUtils.isNotEmpty(body)) {
                JSONObject jsonObject = JSON.parseObject(body);
                JSONArray choices = jsonObject.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String generatedContent = message.getString("content");
                    if (StringUtils.isNotEmpty(generatedContent)) {
                        // 4. 记录生成历史
                        AiPromptGeneration generation = new AiPromptGeneration();
                        generation.setUserInput(userInput);
                        generation.setGeneratedPrompt(generatedContent);
                        generation.setTemplateType(templateType);
                        generation.setLlmConfigId(llmConfig.getConfigId());
                        generation.setModelName(llmConfig.getDefaultModel());
                        generation.setCreateBy("admin");
                        generation.setCreateTime(new java.util.Date());
                        promptGenerationMapper.insertAiPromptGeneration(generation);
                        return generatedContent;
                    }
                }
            }
            throw new RuntimeException("LLM返回内容为空");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("AI生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认启用的LLM配置
     */
    private AiLlmConfig getDefaultLlmConfig() {
        AiPromptTemplateQuery tempQuery = new AiPromptTemplateQuery();
        // 查询所有已启用LLM配置，取第一个启用的
        com.ruoyi.system.domain.vo.AiLlmConfigQuery query = new com.ruoyi.system.domain.vo.AiLlmConfigQuery();
        query.setStatus("0");
        List<AiLlmConfig> configs = llmConfigMapper.selectAiLlmConfigList(query);
        for (AiLlmConfig config : configs) {
            if ("1".equals(config.getIsDefault()) && StringUtils.isNotEmpty(config.getApiKey())) {
                return config;
            }
        }
        // 如果没有默认配置，取第一个有API Key的启用配置
        for (AiLlmConfig config : configs) {
            if (StringUtils.isNotEmpty(config.getApiKey())) {
                return config;
            }
        }
        return null;
    }
}
