package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiLlmConfig;
import com.ruoyi.system.domain.vo.AiLlmConfigQuery;
import com.ruoyi.system.mapper.AiLlmConfigMapper;
import com.ruoyi.system.service.IAiLlmConfigService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * LLM大模型配置Service实现
 *
 * @author ruoyi
 */
@Service
public class AiLlmConfigServiceImpl implements IAiLlmConfigService {

    @Autowired
    private AiLlmConfigMapper aiLlmConfigMapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询LLM大模型配置
     */
    @Override
    public AiLlmConfig selectAiLlmConfigById(Long configId) {
        return aiLlmConfigMapper.selectAiLlmConfigById(configId);
    }

    /**
     * 查询LLM大模型配置列表
     */
    @Override
    public List<AiLlmConfig> selectAiLlmConfigList(AiLlmConfigQuery query) {
        return aiLlmConfigMapper.selectAiLlmConfigList(query);
    }

    /**
     * 新增LLM大模型配置
     */
    @Override
    public int insertAiLlmConfig(AiLlmConfig config) {
        // 如果设置为默认，先清除其他默认
        if ("1".equals(config.getIsDefault())) {
            aiLlmConfigMapper.clearDefault();
        }
        return aiLlmConfigMapper.insertAiLlmConfig(config);
    }

    /**
     * 修改LLM大模型配置
     */
    @Override
    public int updateAiLlmConfig(AiLlmConfig config) {
        // 如果设置为默认，先清除其他默认
        if ("1".equals(config.getIsDefault())) {
            aiLlmConfigMapper.clearDefault();
        }
        return aiLlmConfigMapper.updateAiLlmConfig(config);
    }

    /**
     * 删除LLM大模型配置
     */
    @Override
    public int deleteAiLlmConfigById(Long configId) {
        return aiLlmConfigMapper.deleteAiLlmConfigById(configId);
    }

    /**
     * 批量删除LLM大模型配置
     */
    @Override
    public int deleteAiLlmConfigByIds(Long[] configIds) {
        return aiLlmConfigMapper.deleteAiLlmConfigByIds(configIds);
    }

    /**
     * 设置默认配置
     */
    @Override
    @Transactional
    public int setDefault(Long configId) {
        aiLlmConfigMapper.clearDefault();
        AiLlmConfig config = new AiLlmConfig();
        config.setConfigId(configId);
        config.setIsDefault("1");
        return aiLlmConfigMapper.updateAiLlmConfig(config);
    }

    /**
     * 测试接入 - 获取可用模型列表
     */
    @Override
    public List<String> testConnection(Long configId) {
        AiLlmConfig config = aiLlmConfigMapper.selectAiLlmConfigById(configId);
        if (config == null || StringUtils.isEmpty(config.getApiKey())) {
            throw new RuntimeException("配置不存在或API Key未设置");
        }

        List<String> models = new ArrayList<>();
        try {
            // 构建请求URL
            String url = config.getBaseUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "models";

            // 发送请求获取模型列表
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(null, headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );

            // 解析响应
            String body = response.getBody();
            if (StringUtils.isNotEmpty(body)) {
                JSONObject jsonObject = JSON.parseObject(body);
                // 尝试从 data 数组中获取模型列表
                if (jsonObject.containsKey("data")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject modelObj = dataArray.getJSONObject(i);
                        // 优先获取 model 字段
                        String modelId = modelObj.getString("id");
                        if (StringUtils.isNotEmpty(modelId)) {
                            models.add(modelId);
                        }
                    }
                }
                // OpenAI 格式: object = "list"
                if (jsonObject.containsKey("object") && "list".equals(jsonObject.getString("object"))) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject modelObj = dataArray.getJSONObject(i);
                        String modelId = modelObj.getString("id");
                        if (StringUtils.isNotEmpty(modelId)) {
                            models.add(modelId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("测试连接失败: " + e.getMessage());
        }

        return models;
    }
}
