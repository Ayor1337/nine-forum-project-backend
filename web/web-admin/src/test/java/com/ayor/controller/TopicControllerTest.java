package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TopicControllerTest {

    @Test
    void shouldExposeTopicOptionsEndpointInWebAdmin() throws NoSuchMethodException {
        Method method = TopicController.class.getMethod("getTopicsAsOptions", String.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertArrayEquals(new String[]{"/options"}, mapping.value());
    }
}
