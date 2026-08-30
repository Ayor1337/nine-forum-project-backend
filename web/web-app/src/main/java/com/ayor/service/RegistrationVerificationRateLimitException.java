package com.ayor.service;

/**
 * 注册验证邮件配额耗尽。
 */
public class RegistrationVerificationRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public RegistrationVerificationRateLimitException(long retryAfterSeconds) {
        super("请求过于频繁，请稍后重试");
        this.retryAfterSeconds = Math.max(retryAfterSeconds, 1);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
