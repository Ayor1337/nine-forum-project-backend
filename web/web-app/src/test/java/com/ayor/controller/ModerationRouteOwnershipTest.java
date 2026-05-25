package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationRouteOwnershipTest {

    @Test
    void regularControllersShouldNotExposeModerationRoutes() {
        Stream<Class<?>> controllers = Stream.of(ThemeController.class, TopicController.class, TagController.class, ThreadController.class, PostController.class);

        boolean hasModerationRoute = controllers
                .flatMap(controller -> Arrays.stream(controller.getMethods()))
                .flatMap(this::routeValues)
                .anyMatch(route -> route.contains("/perm") || route.contains("announcements"));

        assertTrue(!hasModerationRoute);
    }

    private Stream<String> routeValues(Method method) {
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return Arrays.stream(postMapping.value());
        }

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
