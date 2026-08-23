package com.ayor.controller.exception;

import com.ayor.result.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidateController {
    /**
     * 处理参数校验失败。
     */

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        boolean imageField = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField())
                .anyMatch(field -> field.equals("base64") || field.equals("fileName")
                        || field.endsWith(".base64") || field.endsWith(".fileName"));
        HttpStatus status = imageField ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(Result.fail(203, "请求参数验证有误"));
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
