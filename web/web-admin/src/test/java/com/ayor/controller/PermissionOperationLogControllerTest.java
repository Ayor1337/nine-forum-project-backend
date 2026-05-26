package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PermissionOperationLogControllerTest {

    @Test
    void shouldExposeListEndpointInWebAdmin() throws NoSuchMethodException, NoSuchFieldException {
        RequestMapping requestMapping = PermissionOperationLogController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api/permission_operation_logs"}, requestMapping.value());

        Method method = PermissionOperationLogController.class.getMethod("listPermissionOperationLogs");
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{}, getMapping.value());

        ParameterizedType resultType = (ParameterizedType) method.getGenericReturnType();
        ParameterizedType dataType = (ParameterizedType) resultType.getActualTypeArguments()[0];
        assertThat(dataType.getActualTypeArguments()[0].getTypeName())
                .isEqualTo("com.ayor.entity.vo.PermissionOperationLogVO");
        assertThat(com.ayor.entity.vo.PermissionOperationLogVO.class.getDeclaredField("params").getType())
                .isEqualTo(java.util.Map.class);
    }
}
