package com.ayor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring.security.webauthn")
/**
 * Passkey 相关配置。
 */
public class WebAuthnProperties {

    /**
     * 依赖方 ID。
     */
    private String rpId = "localhost";

    /**
     * 依赖方名称。
     */
    private String rpName = "NineForum";

    /**
     * 允许的前端 origin。
     */
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000"));

    /**
     * challenge 过期时间，单位秒。
     */
    private long challengeExpireSeconds = 300;
}
