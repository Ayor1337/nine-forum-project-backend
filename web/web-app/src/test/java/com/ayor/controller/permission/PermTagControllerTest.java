package com.ayor.controller.permission;

import com.ayor.entity.dto.TagDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermTagControllerTest {

    @Test
    void controllerShouldExposePermissionTopicTagBasePath() {
        RequestMapping mapping = PermTagController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/topic/{topic_id}/tag", mapping.value()[0]);
    }

    @Test
    void insertTagShouldUseBaseRoute() throws NoSuchMethodException {
        Method method = PermTagController.class.getMethod("insertNewTag", Integer.class, TagDTO.class);

        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertEquals(0, mapping.value().length);
    }
}
