package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagControllerTest {

    @Test
    void getTagByTopicIdShouldUseTopicIdPathVariable() throws NoSuchMethodException {
        Method method = TagController.class.getMethod("getTagByTopicId", Integer.class);

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PathVariable pathVariable = method.getParameters()[0].getAnnotation(PathVariable.class);

        assertEquals(0, getMapping.value().length);
        assertEquals("topic_id", pathVariable.name());
    }
}
