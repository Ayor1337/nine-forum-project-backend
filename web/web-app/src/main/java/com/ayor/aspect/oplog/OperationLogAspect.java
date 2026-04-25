package com.ayor.aspect.oplog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 统一处理 {@link OperationLog} 注解的日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;

    /**
     * 在目标方法执行前后记录操作日志。
     *
     * @param joinPoint 切点
     * @param operationLog 操作日志注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String description = operationLog.value().isEmpty() ? method : operationLog.value();
        String params = operationLog.logParams() ? toJson(joinPoint.getArgs()) : "[ignored]";
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            if (operationLog.logResult()) {
                log.info("Operation [{}] succeeded in {} ms - method: {}, params: {}, result: {}",
                        description, duration, method, params, toJson(result));
            } else {
                log.info("Operation [{}] succeeded in {} ms - method: {}, params: {}",
                        description, duration, method, params);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("Operation [{}] failed in {} ms - method: {}, params: {}, error: {}",
                    description, duration, method, params, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            if (value instanceof Object[] objects) {
                return Arrays.toString(objects);
            }
            return String.valueOf(value);
        }
    }
}
