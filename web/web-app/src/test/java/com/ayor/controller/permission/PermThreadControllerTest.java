package com.ayor.controller.permission;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermThreadControllerTest {

    @Test
    void controllerShouldExposeModerationBasePath() {
        RequestMapping mapping = PermThreadController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/moderation", mapping.value()[0]);
    }

    @Test
    void updateTagShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "updateTag",
                Integer.class,
                Integer.class,
                com.ayor.entity.dto.TagUpdateDTO.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/threads/{thread_id}/tag", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
    }

    @Test
    void deleteThreadTagShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "deleteThreadTag",
                Integer.class,
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/threads/{thread_id}/tag", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
    }

    @Test
    void removeThreadByIdPermissionShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "removeThreadByIdPermission",
                Integer.class,
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/threads/{thread_id}", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
    }

    private Set<String> queryParamNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(this::requestParamName)
                .collect(Collectors.toSet());
    }

    private String requestParamName(RequestParam requestParam) {
        if (!requestParam.value().isEmpty()) {
            return requestParam.value();
        }
        return requestParam.name();
    }
}
