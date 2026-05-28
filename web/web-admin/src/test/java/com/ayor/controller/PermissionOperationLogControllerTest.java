package com.ayor.controller;

import com.ayor.entity.PageEntity;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PermissionOperationLogControllerTest {

    @Test
    void shouldExposeListEndpointInWebAdmin() throws NoSuchMethodException, NoSuchFieldException {
        RequestMapping requestMapping = PermissionOperationLogController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api/permission_operation_logs"}, requestMapping.value());

        Method method = PermissionOperationLogController.class.getMethod(
                "listPermissionOperationLogs",
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                Long.class,
                String.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{}, getMapping.value());

        ParameterizedType resultType = (ParameterizedType) method.getGenericReturnType();
        ParameterizedType dataType = (ParameterizedType) resultType.getActualTypeArguments()[0];
        assertThat(dataType.getRawType()).isEqualTo(PageEntity.class);
        assertThat(dataType.getActualTypeArguments()[0].getTypeName())
                .isEqualTo("com.ayor.entity.vo.PermissionOperationLogVO");
        assertRequestParam(method, 0, "page_num", "1", false);
        assertRequestParam(method, 1, "page_size", "10", false);
        assertRequestParam(method, 2, "action", ValueConstants.DEFAULT_NONE, false);
        assertRequestParam(method, 3, "username", ValueConstants.DEFAULT_NONE, false);
        assertRequestParam(method, 4, "target_type", ValueConstants.DEFAULT_NONE, false);
        assertRequestParam(method, 5, "target_id", ValueConstants.DEFAULT_NONE, false);
        assertRequestParam(method, 6, "sort_order", "desc", false);
        assertThat(com.ayor.entity.vo.PermissionOperationLogVO.class.getDeclaredField("userId").getType())
                .isEqualTo(Integer.class);
        assertThat(com.ayor.entity.vo.PermissionOperationLogVO.class.getDeclaredField("params").getType())
                .isEqualTo(java.util.Map.class);
    }

    @Test
    void shouldExposeOperationOptionsEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionOperationLogController.class.getMethod("listPermissionOptions");
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertArrayEquals(new String[]{"/operation/options"}, mapping.value());
    }

    @Test
    void shouldExposeUsernameOptionsEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionOperationLogController.class.getMethod("listUsernameOptions");
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertArrayEquals(new String[]{"/username/options"}, mapping.value());
    }

    private void assertRequestParam(Method method,
                                    int index,
                                    String name,
                                    String defaultValue,
                                    boolean required) {
        AnnotatedElement parameter = method.getParameters()[index];
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        assertThat(requestParam).isNotNull();
        assertThat(requestParam.value()).isEqualTo(name);
        assertThat(requestParam.defaultValue()).isEqualTo(defaultValue);
        assertThat(requestParam.required()).isEqualTo(required);
    }
}
