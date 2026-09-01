package com.ayor.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorsPropertiesTest {

    @Test
    void shouldReturnExplicitAllowedOrigins() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:10072"));

        assertEquals(List.of("http://localhost:10072"), properties.getAllowedOrigins());
    }

    @Test
    void shouldRejectEmptyOrWildcardOrigins() {
        CorsProperties properties = new CorsProperties();
        assertThrows(IllegalStateException.class, properties::getAllowedOrigins);

        properties.setAllowedOrigins(List.of("*"));
        assertThrows(IllegalStateException.class, properties::getAllowedOrigins);
    }
}
