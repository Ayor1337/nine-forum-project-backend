package com.ayor.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicControllerTest {

    // 测试前台不暴露主题选项接口
    @Test
    void shouldNotExposeTopicOptionsEndpointInWebApp() {
        assertThrows(NoSuchMethodException.class, () -> TopicController.class.getMethod("getTopicOptions"));
    }
}
