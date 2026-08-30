package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.service.AuthorizationService;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StompAuthInterceptorTest {

    @Test
    void shouldAllowGuestConnectWithoutToken() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertDoesNotThrow(() -> interceptor.preSend(message(StompCommand.CONNECT, null, null, "/forum"), mock(MessageChannel.class)));
    }

    @Test
    void shouldRejectInvalidConnectToken() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        when(jwtUtils.resolveJwt("Bearer invalid")).thenReturn(null);
        StompAuthInterceptor interceptor = new StompAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authorizationService", mock(AuthorizationService.class));
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtils);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.CONNECT, null, null, "/forum", "Bearer invalid"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldBindAuthenticatedUserForValidConnectToken() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        DecodedJWT jwt = mock(DecodedJWT.class);
        UserDetails user = User.withUsername("10").password("n/a").authorities("USER").build();
        when(jwtUtils.resolveJwt("Bearer valid")).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(user);
        StompAuthInterceptor interceptor = new StompAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authorizationService", mock(AuthorizationService.class));
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtils);
        Message<?> result = interceptor.preSend(
                message(StompCommand.CONNECT, null, null, "/chat", "Bearer valid"),
                mock(MessageChannel.class)
        );

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertEquals("10", accessor.getUser().getName());
        assertEquals("10", accessor.getSessionAttributes().get("accountId"));
    }

    @Test
    void shouldAllowGuestSubscriptionToPublicForumBroadcast() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/broadcast/forum/topics/7/threads", null, "/forum"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldAllowGuestSubscriptionToPublicPageBroadcast() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/broadcast/page/home", null, "/forum"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldRejectGuestBrokerSend() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SEND, "/broadcast/forum/topics/7/threads", null, "/forum"),
                mock(MessageChannel.class)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/broadcast/topic/7",
            "/transfer/conversation/7",
            "/notif/system",
            "/verify/token-id",
            "/user/notif/presence",
            "/user/transfer/conversation/7"
    })
    void shouldRejectAllBrokerAndUserDestinationsFromClient(String destination) {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SEND, destination, 10, "/chat"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldRejectAuthenticatedBrokerSend() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SEND, "/verify/token-id", 10, "/system"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldAllowGuestSubscriptionToVerificationResult() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/verify/token-id", null, "/system"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldAuthorizeTypingSendWithConversationContext() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        StompAuthInterceptor interceptor = interceptor(authorizationService);

        interceptor.preSend(message(StompCommand.SEND, "/app/conversations/7/typing", 10, "/chat"), mock(MessageChannel.class));

        verify(authorizationService).assertCanAccessConversation(10, 7);
    }

    @Test
    void shouldRejectGuestTypingSend() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SEND, "/app/conversations/7/typing", null, "/chat"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldRejectTypingSubscriptionWhenConversationAuthorizationFails() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).assertCanAccessConversation(10, 7);
        StompAuthInterceptor interceptor = interceptor(authorizationService);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/transfer/conversation/7/typing", 10, "/chat"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldAllowAuthenticatedPresenceSubscriptionOnSystemEndpoint() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/notif/presence", 10, "/system"),
                mock(MessageChannel.class)
        ));
    }

    @Test
    void shouldRejectUnknownEndpointWhenSubscribing() {
        StompAuthInterceptor interceptor = interceptor(mock(AuthorizationService.class));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/broadcast/forum/topics/7/threads", null, "/unknown"),
                mock(MessageChannel.class)
        ));
    }

    private StompAuthInterceptor interceptor(AuthorizationService authorizationService) {
        StompAuthInterceptor interceptor = new StompAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "authorizationService", authorizationService);
        ReflectionTestUtils.setField(interceptor, "jwtUtil", mock(JWTUtils.class));
        return interceptor;
    }

    private Message<?> message(StompCommand command, String destination, Integer userId, String endpointPath) {
        return message(command, destination, userId, endpointPath, null);
    }

    private Message<?> message(StompCommand command,
                               String destination,
                               Integer userId,
                               String endpointPath,
                               String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        if (userId != null) {
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    User.withUsername(userId.toString()).password("n/a").authorities("USER").build(),
                    null
            ));
        }
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("endpointPath", endpointPath);
        accessor.setSessionAttributes(attributes);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
