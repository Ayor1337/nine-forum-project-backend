package com.ayor.config;

import com.ayor.interceptor.StompAuthInterceptor;
import com.ayor.interceptor.WebsocketHandshakeInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        WebsocketConfiguration configuration = new WebsocketConfiguration(authInterceptor, handshakeInterceptor);
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint(any(String[].class))).thenReturn(registration);
        when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);
        when(registration.addInterceptors(any())).thenReturn(registration);

        configuration.registerStompEndpoints(registry);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(registry).addEndpoint(captor.capture());
        assertArrayEquals(new String[]{"/chatboard", "/chat", "/system", "/forum"}, captor.getValue());
    }

    // 测试论坛端点只允许广播目的地
    @Test
    @SuppressWarnings("unchecked")
    void shouldAllowOnlyBroadcastDestinationForForumEndpoint() throws Exception {
        Field field = StompAuthInterceptor.class.getDeclaredField("ENDPOINT_DEST_WHITELIST");
        field.setAccessible(true);

        Map<String, List<String>> whitelist = (Map<String, List<String>>) field.get(null);

        assertEquals(List.of("/broadcast"), whitelist.get("/forum"));
        assertEquals(List.of("/transfer", "/notif", "/app/conversations"), whitelist.get("/chat"));
    }
}
