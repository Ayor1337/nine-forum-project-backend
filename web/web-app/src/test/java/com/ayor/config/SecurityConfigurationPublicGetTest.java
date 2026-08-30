package com.ayor.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigurationPublicGetTest {

    // 测试允许公开全局公告接口
    @Test
    void shouldAllowPublicGlobalAnnouncementEndpoint() throws Exception {
        Field field = SecurityConfiguration.class.getDeclaredField("PUBLIC_GET_ENDPOINTS");
        field.setAccessible(true);

        String[] endpoints = (String[]) field.get(null);

        assertTrue(Arrays.asList(endpoints).contains("/api/announcements/global"));
    }

    // 测试允许公开论坛实时更新 STOMP 握手端点
    @Test
    void shouldAllowPublicForumWebsocketEndpoint() throws Exception {
        Field field = SecurityConfiguration.class.getDeclaredField("PUBLIC_PAGE_ENDPOINTS");
        field.setAccessible(true);

        String[] endpoints = (String[]) field.get(null);

        assertTrue(Arrays.asList(endpoints).contains("/forum"));
    }
}
