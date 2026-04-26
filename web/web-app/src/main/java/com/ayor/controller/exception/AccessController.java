package com.ayor.controller.exception;

import com.ayor.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AccessController {
    /**
     * 处理访问被拒绝异常。
     */

    @ExceptionHandler(value = AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Resolve [{} : {}]", e.getClass(), e.getMessage());
        return Result.fail(403, "权限不足");
    }
}
