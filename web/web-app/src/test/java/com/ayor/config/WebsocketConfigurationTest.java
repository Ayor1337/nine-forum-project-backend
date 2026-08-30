package com.ayor.config;

import com.ayor.interceptor.StompAuthInterceptor;
import com.ayor.interceptor.WebsocketHandshakeInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebsocketConfigurationTest {

    // 测试注册论坛实时更新 STOMP 端点
    @Test
    void shouldRegisterForumEndpoint() {
        StompAuthInterceptor authInterceptor = mock(StompAuthInterceptor.class);
        WebsocketHandshakeInterceptor handshakeInterceptor = mock(WebsocketHandshakeInterceptor.class);
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:3000"));
        WebsocketConfiguration configuration = new WebsocketConfiguration(authInterceptor, handshakeInterceptor, corsProperties);
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint(any(String[].class))).thenReturn(registration);
        when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);
        when(registration.addInterceptors(any())).thenReturn(registration);

        configuration.registerStompEndpoints(registry);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(registry).addEndpoint(captor.capture());
        assertArrayEquals(new String[]{"/chatboard", "/chat", "/system", "/forum"}, captor.getValue());
        verify(registration).setAllowedOrigins("http://localhost:3000");
    }
}
