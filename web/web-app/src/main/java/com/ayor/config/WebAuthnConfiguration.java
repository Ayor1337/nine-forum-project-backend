package com.ayor.config;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebAuthnProperties.class)
/**
 * WebAuthn4J 基础配置。
 */
public class WebAuthnConfiguration {

    /**
     * 创建 WebAuthn4J 使用的对象转换器。
     *
     * @return object converter
     */
    @Bean
    ObjectConverter webAuthnObjectConverter() {
        return new ObjectConverter();
    }

    /**
     * 创建 WebAuthn4J 管理器。
     *
     * @param objectConverter 对象转换器
     * @return WebAuthnManager
     */
    @Bean
    WebAuthnManager webAuthnManager(ObjectConverter objectConverter) {
        return WebAuthnManager.createNonStrictWebAuthnManager(objectConverter);
    }
}
