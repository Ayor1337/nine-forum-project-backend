package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    @Resource
    private JWTUtils jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (acc.getCommand() == StompCommand.CONNECT) {
            String authorization = acc.getFirstNativeHeader("Authorization"); // 来自 STOMP connectHeaders
            DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
            if (jwt == null) {
                throw new IllegalArgumentException("Invalid or missing Authorization");
            }

            UserDetails user = jwtUtil.toUser(jwt);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            acc.setUser(authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        return MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
    }
}


