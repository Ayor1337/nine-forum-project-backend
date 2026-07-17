package com.ayor.interceptor;

import com.ayor.service.AuthorizationService;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StompAuthInterceptorTest {

    // 测试 typing 发送复用会话成员鉴权
    @Test
    void shouldAuthorizeTypingSendWithConversationContext() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        StompAuthInterceptor interceptor = interceptor(authorizationService);
        Message<?> message = message(StompCommand.SEND, "/app/conversations/7/typing", 10, "/chat");

        interceptor.preSend(message, mock(MessageChannel.class));

        verify(authorizationService).assertCanAccessConversation(10, 7);
    }

    // 测试非会话成员不能订阅 typing 目的地
    @Test
    void shouldRejectTypingSubscriptionWhenConversationAuthorizationFails() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).assertCanAccessConversation(10, 7);
        StompAuthInterceptor interceptor = interceptor(authorizationService);
        Message<?> message = message(StompCommand.SUBSCRIBE, "/user/transfer/conversation/7/typing", 10, "/chat");

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, mock(MessageChannel.class)));
    }

    // 测试 presence 通知允许已认证用户订阅
    @Test
    void shouldAllowAuthenticatedPresenceSubscriptionOnSystemEndpoint() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        StompAuthInterceptor interceptor = interceptor(authorizationService);
        Message<?> message = message(StompCommand.SUBSCRIBE, "/user/notif/presence", 10, "/system");

        assertDoesNotThrow(() -> interceptor.preSend(message, mock(MessageChannel.class)));
    }

    private StompAuthInterceptor interceptor(AuthorizationService authorizationService) {
        StompAuthInterceptor interceptor = new StompAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authorizationService", authorizationService);
        ReflectionTestUtils.setField(interceptor, "jwtUtil", mock(JWTUtils.class));
        return interceptor;
    }

    private Message<?> message(StompCommand command, String destination, Integer userId, String endpointPath) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                User.withUsername(userId.toString()).password("n/a").authorities("USER").build(),
                null
        ));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("endpointPath", endpointPath);
        accessor.setSessionAttributes(attributes);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
