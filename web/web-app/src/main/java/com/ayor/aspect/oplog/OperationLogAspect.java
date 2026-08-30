package com.ayor.aspect.oplog;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.mapper.PermissionOperationLogMapper;
import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一处理 {@link OperationLog} 注解的日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    private final PermissionOperationLogMapper operationLogMapper;

    private final SecurityUtils securityUtils;

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
        Map<String, Object> params = operationLog.logParams() ? toParamMap(signature, joinPoint.getArgs()) : Map.of();
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
            saveIfNecessary(joinPoint, operationLog, method, params, duration, result);
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

    private void saveIfNecessary(ProceedingJoinPoint joinPoint,
                                 OperationLog operationLog,
                                 String method,
                                 Map<String, Object> params,
                                 long duration,
                                 Object result) {
        if (!operationLog.save() || !isSuccessfulResult(result)) {
            return;
        }
        try {
            PermissionOperationLog operation = new PermissionOperationLog();
            operation.setUserId(securityUtils.getSecurityUserId());
            operation.setAction(operationLog.action());
            operation.setTargetType(operationLog.targetType());
            operation.setTargetId(resolveTargetId(joinPoint, operationLog.targetIdParam()));
            operation.setMethod(method);
            operation.setParams(params);
            operation.setDurationMs(duration);
            operation.setCreateTime(new Date());
            operationLogMapper.insert(operation);
        } catch (Exception ex) {
            log.error("Failed to save permission operation log - method: {}, error: {}", method, ex.getMessage(), ex);
        }
    }

    private Map<String, Object> toParamMap(MethodSignature signature, Object[] args) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return params;
        }
        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            String name = parameterNames != null && i < parameterNames.length ? parameterNames[i] : "arg" + i;
            putParam(params, name, args[i]);
        }
        return params;
    }

    private void putParam(Map<String, Object> params, String name, Object value) {
        if (value == null || isScalar(value)) {
            params.put(name, value);
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    putWithoutOverwriting(params, String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return;
        }
        if (value instanceof Iterable<?> || value.getClass().isArray()) {
            params.put(name, value);
            return;
        }
        Map<String, Object> fields = objectMapper.convertValue(value, MAP_TYPE);
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            putWithoutOverwriting(params, entry.getKey(), entry.getValue());
        }
    }

    private void putWithoutOverwriting(Map<String, Object> params, String key, Object value) {
        if (!params.containsKey(key)) {
            params.put(key, value);
        }
    }

    private boolean isScalar(Object value) {
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof Date;
    }

    private boolean isSuccessfulResult(Object result) {
        if (!(result instanceof Result<?> response)) {
            return false;
        }
        return ResultCodeEnum.SUCCESS.getCode().equals(response.getCode());
    }

    private Long resolveTargetId(ProceedingJoinPoint joinPoint, String targetIdParam) {
        if (targetIdParam == null || targetIdParam.isBlank()) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (parameterNames == null || args == null) {
            return null;
        }
        for (int i = 0; i < parameterNames.length && i < args.length; i++) {
            if (targetIdParam.equals(parameterNames[i])) {
                return toLong(args[i]);
            }
        }
        return null;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
