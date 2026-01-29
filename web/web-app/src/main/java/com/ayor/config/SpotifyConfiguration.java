package com.ayor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spotify配置类
 * 配置WebClient用于调用Spotify API
 */
@Configuration
public class SpotifyConfiguration {

    /**
     * 创建WebClient Bean用于HTTP请求
     * @return WebClient实例
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB缓冲区
                .build();
    }
}
