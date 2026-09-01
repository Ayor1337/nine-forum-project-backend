package com.ayor.config;

import com.ayor.interceptor.StompAuthInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebsocketConfigurationTest {

    @Test
    void shouldUseConfiguredAllowedOrigins() {
        StompAuthInterceptor authInterceptor = mock(StompAuthInterceptor.class);
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:10072"));
        WebsocketConfiguration configuration = new WebsocketConfiguration(authInterceptor, corsProperties);
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint(any(String[].class))).thenReturn(registration);
        when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);

        configuration.registerStompEndpoints(registry);

        verify(registration).setAllowedOrigins("http://localhost:10072");
    }
}
