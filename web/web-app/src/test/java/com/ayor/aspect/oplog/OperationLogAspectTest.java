package com.ayor.aspect.oplog;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.mapper.PermissionOperationLogMapper;
import com.ayor.result.Result;
import com.ayor.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogAspectTest {

    @Mock
    private PermissionOperationLogMapper operationLogMapper;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Test
    void shouldSaveOperationLogWhenResultIsSuccessful() throws Throwable {
        OperationLogAspect aspect = new OperationLogAspect(new ObjectMapper(), operationLogMapper, securityUtils);
        OperationLog operationLog = TestOperations.class
                .getMethod("successfulOperation", Integer.class, String.class)
                .getAnnotation(OperationLog.class);
        Result<Void> result = Result.ok();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(TestOperations.class);
        when(signature.getName()).thenReturn("successfulOperation");
        when(signature.getParameterNames()).thenReturn(new String[]{"topicId", "name"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{7, "java"});
        when(joinPoint.proceed()).thenReturn(result);
        when(securityUtils.getSecurityUserId()).thenReturn(42);

        Object actual = aspect.around(joinPoint, operationLog);

        assertSame(result, actual);
        ArgumentCaptor<PermissionOperationLog> captor = ArgumentCaptor.forClass(PermissionOperationLog.class);
        verify(operationLogMapper).insert(captor.capture());
        PermissionOperationLog saved = captor.getValue();
        assertEquals(42, saved.getUserId());
        assertEquals("UPDATE_TOPIC", saved.getAction());
        assertEquals("topic", saved.getTargetType());
        assertEquals(7L, saved.getTargetId());
        assertEquals("TestOperations.successfulOperation", saved.getMethod());
    }

    @Test
    void shouldNotSaveOperationLogWhenResultFails() throws Throwable {
        OperationLogAspect aspect = new OperationLogAspect(new ObjectMapper(), operationLogMapper, securityUtils);
        OperationLog operationLog = TestOperations.class
                .getMethod("successfulOperation", Integer.class, String.class)
                .getAnnotation(OperationLog.class);
        Result<Void> result = Result.fail();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(TestOperations.class);
        when(signature.getName()).thenReturn("successfulOperation");
        when(joinPoint.getArgs()).thenReturn(new Object[]{7, "java"});
        when(joinPoint.proceed()).thenReturn(result);

        Object actual = aspect.around(joinPoint, operationLog);

        assertSame(result, actual);
        verify(operationLogMapper, never()).insert(any(PermissionOperationLog.class));
    }

    @Test
    void shouldNotSaveOperationLogWhenTargetThrows() throws Throwable {
        OperationLogAspect aspect = new OperationLogAspect(new ObjectMapper(), operationLogMapper, securityUtils);
        OperationLog operationLog = TestOperations.class
                .getMethod("successfulOperation", Integer.class, String.class)
                .getAnnotation(OperationLog.class);
        IllegalStateException exception = new IllegalStateException("boom");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(TestOperations.class);
        when(signature.getName()).thenReturn("successfulOperation");
        when(joinPoint.getArgs()).thenReturn(new Object[]{7, "java"});
        when(joinPoint.proceed()).thenThrow(exception);

        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> aspect.around(joinPoint, operationLog));

        assertSame(exception, actual);
        verify(operationLogMapper, never()).insert(any(PermissionOperationLog.class));
    }

    @Test
    void shouldKeepBusinessResultWhenSavingLogFails() throws Throwable {
        OperationLogAspect aspect = new OperationLogAspect(new ObjectMapper(), operationLogMapper, securityUtils);
        OperationLog operationLog = TestOperations.class
                .getMethod("successfulOperation", Integer.class, String.class)
                .getAnnotation(OperationLog.class);
        Result<Void> result = Result.ok();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(TestOperations.class);
        when(signature.getName()).thenReturn("successfulOperation");
        when(signature.getParameterNames()).thenReturn(new String[]{"topicId", "name"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{7, "java"});
        when(joinPoint.proceed()).thenReturn(result);
        when(securityUtils.getSecurityUserId()).thenReturn(42);
        doThrow(new IllegalStateException("database unavailable"))
                .when(operationLogMapper).insert(any(PermissionOperationLog.class));

        Object actual = aspect.around(joinPoint, operationLog);

        assertSame(result, actual);
    }

    private static class TestOperations {

        @OperationLog(value = "更新话题", save = true, action = "UPDATE_TOPIC", targetType = "topic", targetIdParam = "topicId")
        public void successfulOperation(Integer topicId, String name) {
        }
    }
}
