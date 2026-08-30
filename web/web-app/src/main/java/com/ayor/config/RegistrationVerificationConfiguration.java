package com.ayor.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 启用注册验证邮件配置绑定。
 */
@Configuration
@EnableConfigurationProperties(RegistrationVerificationProperties.class)
public class RegistrationVerificationConfiguration {
}
