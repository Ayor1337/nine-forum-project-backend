package com.ayor.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 注册验证邮件的幂等、配额和告警配置。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "nine-forum.registration-verification")
public class RegistrationVerificationProperties {

    @Min(1)
    private long idempotencySeconds = 60;

    @Min(1)
    private int emailLimit = 3;

    @Min(1)
    private long emailWindowSeconds = 900;

    @Min(1)
    private int ipLimit = 10;

    @Min(1)
    private long ipWindowSeconds = 900;

    @Min(1)
    private int globalLimit = 100;

    @Min(1)
    private long globalWindowSeconds = 600;

    @Min(1)
    @Max(100)
    private int globalAlertPercent = 80;
}
