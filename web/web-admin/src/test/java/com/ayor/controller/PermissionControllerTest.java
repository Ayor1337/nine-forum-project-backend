package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionControllerTest {

    // 测试后台暴露批量更新接口
    @Test
    void shouldExposeBatchUpdateEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionController.class.getMethod("updatePermissions", List.class);
        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertArrayEquals(new String[]{"/batch"}, mapping.value());
    }

    // 测试后台暴露批量删除接口
    @Test
    void shouldExposeBatchDeleteEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionController.class.getMethod("deletePermissions", List.class);
        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertArrayEquals(new String[]{"/batch"}, mapping.value());
    }

    // 测试不暴露权限操作日志选项在权限控制器
    @Test
    void shouldNotExposePermissionOperationLogOptionsInPermissionController() {
        assertThrows(NoSuchMethodException.class, () -> PermissionController.class.getMethod("listPermissionOptions"));
        assertThrows(NoSuchMethodException.class, () -> PermissionController.class.getMethod("listPermissionOperationUsernameOptions"));
    }
}
