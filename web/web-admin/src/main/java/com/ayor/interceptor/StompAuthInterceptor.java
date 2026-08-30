package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.mapper.RoleMapper;
import com.ayor.util.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String REQUIRED_ROLE = "OWNER";
    private static final String REQUIRED_AUTHORITY = "ROLE_OWNER";
    private static final String REPORTS_DESTINATION = "/topic/reports";
    private static final String USERNAME_SESSION_ATTRIBUTE = "adminUsername";

    private final JWTUtils jwtUtils;
    private final RoleMapper roleMapper;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (!StringUtils.hasText(authorization)) {
                throw new AccessDeniedException("未授权连接");
            }
            DecodedJWT jwt = resolveJwt(authorization);
            if (jwt == null) {
                throw new AccessDeniedException("未授权连接");
            }
            String username = resolveUsername(jwt);
            if (!StringUtils.hasText(username) || !isOwner(username)) {
                throw new AccessDeniedException("权限不足");
            }
            UserDetails user = toUser(jwt);
            if (user == null) {
                throw new AccessDeniedException("未授权连接");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(new SimpleGrantedAuthority(REQUIRED_AUTHORITY))
            );
            accessor.setUser(authentication);
            sessionAttributes(accessor).put(USERNAME_SESSION_ATTRIBUTE, username);
            return message;
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            Map<String, Object> attributes = accessor.getSessionAttributes();
            Object usernameAttribute = attributes == null ? null : attributes.get(USERNAME_SESSION_ATTRIBUTE);
            if (accessor.getUser() == null || !(usernameAttribute instanceof String username)
                    || !StringUtils.hasText(username)) {
                throw new AccessDeniedException("未授权订阅");
            }
            if (!isOwner(username)) {
                throw new AccessDeniedException("权限不足");
            }
            String destination = accessor.getDestination();
            if (!REPORTS_DESTINATION.equals(destination)) {
                throw new AccessDeniedException("无权订阅该地址");
            }
        }
        return message;
    }

    private DecodedJWT resolveJwt(String authorization) {
        try {
            return jwtUtils.resolveJwt(authorization);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String resolveUsername(DecodedJWT jwt) {
        try {
            return jwt.getClaim("name").asString();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private UserDetails toUser(DecodedJWT jwt) {
        try {
            return jwtUtils.toUser(jwt);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private boolean isOwner(String username) {
        try {
            return REQUIRED_ROLE.equals(roleMapper.getRoleNameByUsername(username));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Map<String, Object> sessionAttributes(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
            accessor.setSessionAttributes(attributes);
        }
        return attributes;
    }
}
