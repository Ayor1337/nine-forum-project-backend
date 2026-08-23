package com.ayor.web;

import com.ayor.image.ImageProcessingBusyException;
import com.ayor.image.ImageValidationException;
import com.ayor.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ImageUploadExceptionHandler {

    @ExceptionHandler(ImageValidationException.class)
    public ResponseEntity<Result<Void>> handleImageValidation(ImageValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(203, exception.getMessage()));
    }

    @ExceptionHandler(ImageProcessingBusyException.class)
    public ResponseEntity<Result<Void>> handleImageProcessingBusy(ImageProcessingBusyException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Result.fail(429, exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        if (containsTooLargeException(exception)) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Result.fail(413, "请求体过大"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(203, "请求参数内容有误"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(203, "请求参数验证有误"));
    }

    private boolean containsTooLargeException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RequestBodyTooLargeException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
