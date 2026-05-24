package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Web相关配置
 *
 * @author ruoyi
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate配置
     * 用于调用外部LLM API
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 连接超时 10秒
        factory.setReadTimeout(30000);      // 读取超时 30秒
        return new RestTemplate(factory);
    }
}
