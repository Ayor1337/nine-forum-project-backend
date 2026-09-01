package com.ayor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 与 WebSocket 共用的跨域来源配置。
 */
@Data
@ConfigurationProperties(prefix = "nine-forum.cors")
public class CorsProperties {

    /**
     * 明确允许访问管理端接口与 WebSocket 的来源。
     */
    private List<String> allowedOrigins = new ArrayList<>();

    public List<String> getAllowedOrigins() {
        if (allowedOrigins.isEmpty() || allowedOrigins.stream().anyMatch(this::isUnsafeOrigin)) {
            throw new IllegalStateException("必须配置不含通配符的跨域来源");
        }
        return List.copyOf(allowedOrigins);
    }

    private boolean isUnsafeOrigin(String origin) {
        return origin == null || origin.isBlank() || origin.contains("*");
    }
}
