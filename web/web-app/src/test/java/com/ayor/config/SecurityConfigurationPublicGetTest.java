package com.ayor.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigurationPublicGetTest {

    @Test
    void shouldAllowPublicGlobalAnnouncementEndpoint() throws Exception {
        Field field = SecurityConfiguration.class.getDeclaredField("PUBLIC_GET_ENDPOINTS");
        field.setAccessible(true);

        String[] endpoints = (String[]) field.get(null);

        assertTrue(Arrays.asList(endpoints).contains("/api/announcements/global"));
    }
}
