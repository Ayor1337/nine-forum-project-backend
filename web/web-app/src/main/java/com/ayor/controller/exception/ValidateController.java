package com.ayor.controller.exception;

import com.ayor.result.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ValidateController {
    /**
     * 处理参数校验失败。
     */

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        return Result.fail(203, "请求参数验证有误");
    }
    /**
     * 处理缺少请求参数异常。
     */

    @ExceptionHandler(value = MissingRequestValueException.class)
    public Result<Void> handleMissingRequestValueException(MissingRequestValueException e) {
        return Result.fail(203, "请求参数内容有误");
    }

    /**
     * 处理请求参数类型转换失败。
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return Result.fail(203, "请求参数内容有误");
    }

}
