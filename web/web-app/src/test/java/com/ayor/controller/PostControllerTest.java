package com.ayor.controller;

import com.ayor.entity.dto.PostEditDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostControllerTest {

    // 测试编辑帖子暴露用户编辑路由
    @Test
    void editPostShouldExposeUserEditRoute() throws NoSuchMethodException {
        Method method = PostController.class.getMethod(
                "editPost",
                Integer.class,
                PostEditDTO.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/posts/{post_id}", mapping.value()[0]);
    }

    // 测试获取帖子编辑历史暴露公开历史路由
    @Test
    void getPostEditHistoryShouldExposePublicHistoryRoute() throws NoSuchMethodException {
        Method method = PostController.class.getMethod(
                "getPostEditHistory",
                Integer.class
        );

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertEquals("/posts/{post_id}/edit-history", mapping.value()[0]);
    }
}
