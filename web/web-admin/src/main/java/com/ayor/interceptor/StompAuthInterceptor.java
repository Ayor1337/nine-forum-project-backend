package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    @Resource
    private JWTUtils jwtUtils;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            DecodedJWT jwt = jwtUtils.resolveJwt(authorization);
            if (jwt == null) {
                throw new AccessDeniedException("未授权连接");
            }
            UserDetails user = jwtUtils.toUser(jwt);
            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            return message;
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null) {
                throw new AccessDeniedException("未授权订阅");
            }
            String destination = accessor.getDestination();
            if (!"/topic/reports".equals(destination)) {
                throw new AccessDeniedException("无权订阅该地址");
            }
        }
        return message;
    }
}
