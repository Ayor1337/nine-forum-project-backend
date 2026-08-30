package com.ayor.controller.exception;

import com.ayor.result.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccessController {
    /**
     * 处理访问被拒绝异常。
     */

    @ExceptionHandler(value = AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(403, "权限不足");
    }

    @ExceptionHandler(value = AuthenticationCredentialsNotFoundException.class)
    public Result<Void> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {
        return Result.fail(401, e.getMessage());
    }
}
