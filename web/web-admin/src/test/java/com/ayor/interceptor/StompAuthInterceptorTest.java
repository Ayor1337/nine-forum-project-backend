package com.ayor.interceptor;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.mapper.RoleMapper;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StompAuthInterceptorTest {

    private static final String OWNER_USERNAME = "owner";
    private static final String AUTHORIZATION = "Bearer valid";

    @Test
    void shouldRejectConnectWithoutAuthorization() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(null), mock(MessageChannel.class)));

        verify(jwtUtils, never()).resolveJwt(null);
        verifyNoRoleLookup(roleMapper);
    }

    @Test
    void shouldRejectInvalidConnectToken() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(null);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));

        verifyNoRoleLookup(roleMapper);
    }

    @Test
    void shouldRejectConnectWhenJwtResolutionFailsClosed() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenThrow(new IllegalStateException("resolver unavailable"));
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));

        assertEquals("未授权连接", exception.getMessage());
        verifyNoRoleLookup(roleMapper);
    }

    @Test
    void shouldRejectValidUserTokenEvenWhenTokenClaimsOwnerAuthority() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = jwt(OWNER_USERNAME);
        UserDetails tokenUser = User.withUsername("7")
                .password("n/a")
                .authorities("ROLE_OWNER")
                .build();
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(tokenUser);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn("USER");
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));

        assertEquals("权限不足", exception.getMessage());
        verify(jwtUtils, never()).toUser(jwt);
    }

    @Test
    void shouldRejectConnectWhenTokenHasNoUsernameClaim() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(jwt.getClaim("name")).thenReturn(null);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));

        verifyNoRoleLookup(roleMapper);
        verify(jwtUtils, never()).toUser(jwt);
    }

    @Test
    void shouldAllowOwnerConnectWithMinimalAuthorityAndSessionUsername() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = jwt(OWNER_USERNAME);
        UserDetails tokenUser = User.withUsername("7")
                .password("n/a")
                .authorities("ROLE_USER", "PERM_REPORT_READ")
                .build();
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(tokenUser);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn("OWNER");
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        Message<?> result = interceptor.preSend(
                connectMessage(AUTHORIZATION), mock(MessageChannel.class));

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertEquals("7", accessor.getUser().getName());
        Authentication authentication = (Authentication) accessor.getUser();
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OWNER")));
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals(OWNER_USERNAME, accessor.getSessionAttributes().get("adminUsername"));
        assertFalse(accessor.getSessionAttributes().containsValue(AUTHORIZATION));
        verify(roleMapper).getRoleNameByUsername(OWNER_USERNAME);
    }

    @Test
    void shouldRejectConnectWhenCurrentRoleIsMissing() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = jwt(OWNER_USERNAME);
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn(null);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));
    }

    @Test
    void shouldRejectWhenRoleLookupFailsClosed() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = jwt(OWNER_USERNAME);
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME))
                .thenThrow(new IllegalStateException("database unavailable"));
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class)));

        assertEquals("权限不足", exception.getMessage());
        verify(jwtUtils, never()).toUser(jwt);
    }

    @Test
    void shouldAllowOwnerSubscriptionToReports() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn("OWNER");
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("adminUsername", OWNER_USERNAME);
        UsernamePasswordAuthenticationToken principal = ownerPrincipal();

        Message<?> result = interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/topic/reports", principal, sessionAttributes),
                mock(MessageChannel.class));

        assertNotNull(result);
        verify(roleMapper).getRoleNameByUsername(OWNER_USERNAME);
    }

    @Test
    void shouldRejectSubscriptionAfterOwnerRoleIsRevoked() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        DecodedJWT jwt = jwt(OWNER_USERNAME);
        UserDetails tokenUser = User.withUsername("7").password("n/a").build();
        when(jwtUtils.resolveJwt(AUTHORIZATION)).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(tokenUser);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn("OWNER", "USER");
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);
        Message<?> connect = interceptor.preSend(connectMessage(AUTHORIZATION), mock(MessageChannel.class));
        StompHeaderAccessor connectedAccessor = StompHeaderAccessor.wrap(connect);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(
                        message(StompCommand.SUBSCRIBE, "/topic/reports", connectedAccessor.getUser(),
                                connectedAccessor.getSessionAttributes()),
                        mock(MessageChannel.class)));

        assertEquals("权限不足", exception.getMessage());
        verify(roleMapper, times(2)).getRoleNameByUsername(OWNER_USERNAME);
    }

    @Test
    void shouldRejectSubscriptionToOtherDestination() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        when(roleMapper.getRoleNameByUsername(OWNER_USERNAME)).thenReturn("OWNER");
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("adminUsername", OWNER_USERNAME);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(
                        message(StompCommand.SUBSCRIBE, "/topic/reports/extra", ownerPrincipal(), sessionAttributes),
                        mock(MessageChannel.class)));

        assertEquals("无权订阅该地址", exception.getMessage());
    }

    @Test
    void shouldRejectSubscriptionWithoutConnectSession() {
        JWTUtils jwtUtils = mock(JWTUtils.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtils, roleMapper);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(
                        message(StompCommand.SUBSCRIBE, "/topic/reports", ownerPrincipal(), new HashMap<>()),
                        mock(MessageChannel.class)));

        verifyNoRoleLookup(roleMapper);
    }

    private DecodedJWT jwt(String username) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(claim.asString()).thenReturn(username);
        when(jwt.getClaim("name")).thenReturn(claim);
        return jwt;
    }

    private UsernamePasswordAuthenticationToken ownerPrincipal() {
        return new UsernamePasswordAuthenticationToken(
                User.withUsername("7").password("n/a").build(),
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_OWNER"))
        );
    }

    private Message<?> message(StompCommand command,
                               String destination,
                               Principal principal,
                               Map<String, Object> sessionAttributes) {
        return message(command, destination, principal, sessionAttributes, null);
    }

    private Message<?> message(StompCommand command,
                               String destination,
                               Principal principal,
                               Map<String, Object> sessionAttributes,
                               String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        if (principal != null) {
            accessor.setUser(principal);
        }
        if (sessionAttributes != null) {
            accessor.setSessionAttributes(sessionAttributes);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> connectMessage(String authorization) {
        return message(StompCommand.CONNECT, null, null, new HashMap<>(), authorization);
    }

    private void verifyNoRoleLookup(RoleMapper roleMapper) {
        verify(roleMapper, never()).getRoleNameByUsername(org.mockito.ArgumentMatchers.anyString());
    }
}
