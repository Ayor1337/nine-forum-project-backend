package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationRouteOwnershipTest {

    @Test
    void regularControllersShouldNotExposeModerationRoutes() {
        Stream<Class<?>> controllers = Stream.of(ThreadController.class, PostController.class);

        boolean hasModerationRoute = controllers
                .flatMap(controller -> Arrays.stream(controller.getMethods()))
                .flatMap(this::routeValues)
                .anyMatch(route -> route.contains("/moderation"));

        assertTrue(!hasModerationRoute);
    }

    private Stream<String> routeValues(Method method) {
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return Arrays.stream(putMapping.value());
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return Arrays.stream(deleteMapping.value());
        }

        return Stream.empty();
    }
}
