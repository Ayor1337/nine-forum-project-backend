package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    @Resource
    private JWTUtils jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null || acc.getCommand() == null) {
            return message;
        }

        switch (acc.getCommand()) {
            case CONNECT -> {
                String authorization = acc.getFirstNativeHeader("Authorization"); // 来自 STOMP connectHeaders
                DecodedJWT jwt = jwtUtil.resolveJwt(authorization);

                if (jwt != null) {
                    UserDetails user = jwtUtil.toUser(jwt);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    acc.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            case SUBSCRIBE -> {
                Principal principal = acc.getUser();
                String dest = acc.getDestination();
                if (!canSubscribe(principal, dest)) {
                    throw new AccessDeniedException("无权查看消息");
                }
            }
            case SEND -> {
                Principal principal = acc.getUser();
                String dest = acc.getDestination();
                if (!canSend(principal, dest)) {
                    throw new AccessDeniedException("无权发送消息");
                }
            }
            default -> {
                return message;
            }
        }
        return message;
    }

    private boolean canSubscribe(Principal p, String destination) {
        if (destination == null) {
            return false;
        }
        if (destination.contains("/verify")) {
            return true;
        }
        if (destination.contains("/broadcast")) {
            return true;
        }
        if (destination.contains("/transfer")) {
            return p instanceof UsernamePasswordAuthenticationToken;
        }
        if (destination.contains("/notif")) {
            return p instanceof UsernamePasswordAuthenticationToken;
        }
        return false;
    }

    private boolean canSend(Principal p, String destination) {
        return true;
    }
}


