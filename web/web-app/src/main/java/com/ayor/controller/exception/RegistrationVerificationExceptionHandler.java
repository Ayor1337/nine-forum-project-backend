package com.ayor.controller.exception;

import com.ayor.result.Result;
import com.ayor.service.RegistrationVerificationRateLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 注册验证邮件限流的 HTTP 映射。
 */
@RestControllerAdvice
public class RegistrationVerificationExceptionHandler {

    @ExceptionHandler(RegistrationVerificationRateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimit(RegistrationVerificationRateLimitException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.getRetryAfterSeconds()))
                .body(Result.fail(429, "请求过于频繁，请稍后重试"));
    }
}
