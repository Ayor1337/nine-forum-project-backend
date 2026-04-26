package com.ayor.controller.exception;

import com.ayor.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ValidateController {
    /**
     * 处理参数校验失败。
     */

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Resolve [{} : {}]", e.getClass(), e.getMessage());
        return Result.fail(203, "请求参数验证有误");
    }
    /**
     * 处理缺少请求参数异常。
     */

    @ExceptionHandler(value = MissingRequestValueException.class)
    public Result<Void> handleMissingRequestValueException(MissingRequestValueException e) {
        log.warn("Resolve [{} : {}]", e.getClass(), e.getMessage());
        return Result.fail(203, "请求参数内容有误");
    }

}
