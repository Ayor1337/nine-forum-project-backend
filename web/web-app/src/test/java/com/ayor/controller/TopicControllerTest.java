package com.ayor.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicControllerTest {

    @Test
    void shouldNotExposeTopicOptionsEndpointInWebApp() {
        assertThrows(NoSuchMethodException.class, () -> TopicController.class.getMethod("getTopicOptions"));
    }
}
