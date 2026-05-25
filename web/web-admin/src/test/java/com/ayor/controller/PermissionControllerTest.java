package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PermissionControllerTest {

    @Test
    void shouldExposeBatchUpdateEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionController.class.getMethod("updatePermissions", List.class);
        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertArrayEquals(new String[]{"/batch"}, mapping.value());
    }

    @Test
    void shouldExposeBatchDeleteEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = PermissionController.class.getMethod("deletePermissions", List.class);
        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertArrayEquals(new String[]{"/batch"}, mapping.value());
    }
}
