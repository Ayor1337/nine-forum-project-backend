package com.ayor.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.service.AuthorizationService;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private AuthorizationService authorizationService;

    /**
     * Map.of 方法。
     */

    private static final Map<String, List<String>> ENDPOINT_DEST_WHITELIST = Map.of(
            "/chatboard", List.of("/broadcast"),
            "/chat", List.of("/transfer", "/notif"),
            "/system", List.of("/notif", "/verify")
    );

    private static final Pattern CONVERSATION_DESTINATION =
            Pattern.compile("^/user(?:/[^/]+)?/transfer/conversation/(\\d+)$|^/transfer/conversation/(\\d+)$");

    /**
     * 在 STOMP 连接、订阅和发送阶段执行鉴权。
     *
     * @param message STOMP 消息
     * @param channel 消息通道
     * @return 原始消息
     */
    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
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
                if (!canSubscribe(principal, dest, acc)) {
                    throw new AccessDeniedException("无权查看消息");
                }
            }
            case SEND -> {
                Principal principal = acc.getUser();
                String dest = acc.getDestination();
                if (!canSend(principal, dest, acc)) {
                    throw new AccessDeniedException("无权发送消息");
                }
            }
            default -> {
                return message;
            }
        }
        return message;
    }

    /**
     * 判断当前用户是否允许订阅指定目的地。
     *
     * @param p 当前主体
     * @param destination 订阅目的地
     * @param accessor STOMP 头访问器
     * @return 允许订阅返回 true
     */
    private boolean canSubscribe(Principal p, String destination, StompHeaderAccessor accessor) {
        if (destination == null) {
            return false;
        }
        if (!matchEndpointDestination(accessor, destination)) {
            return false;
        }
        if (destination.contains("/verify")) {
            return true;
        }
        if (destination.contains("/broadcast")) {
            return true;
        }
        if (destination.contains("/transfer")) {
            Integer userId = resolveUserId(p);
            if (userId == null) {
                return false;
            }
            Integer conversationId = resolveConversationId(destination);
            if (conversationId == null) {
                return false;
            }
            authorizationService.assertCanAccessConversation(userId, conversationId);
            return true;
        }
        if (destination.contains("/notif")) {
            return p instanceof UsernamePasswordAuthenticationToken;
        }
        return false;
    }

    /**
     * 判断当前用户是否允许发送到指定目的地。
     *
     * @param p 当前主体
     * @param destination 发送目的地
     * @param accessor STOMP 头访问器
     * @return 允许发送返回 true
     */
    private boolean canSend(Principal p, String destination, StompHeaderAccessor accessor) {
        if (destination == null) {
            return false;
        }
        if (!matchEndpointDestination(accessor, destination)) {
            return false;
        }
        if (!destination.contains("/transfer")) {
            return true;
        }
        Integer userId = resolveUserId(p);
        if (userId == null) {
            return false;
        }
        Integer conversationId = resolveConversationId(destination);
        if (conversationId == null) {
            return false;
        }
        authorizationService.assertCanAccessConversation(userId, conversationId);
        return true;
    }

    /**
     * 校验目的地是否与当前 WebSocket 端点匹配。
     *
     * @param accessor STOMP 头访问器
     * @param destination 目的地
     * @return 匹配返回 true
     */
    private boolean matchEndpointDestination(StompHeaderAccessor accessor, String destination) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            return true;
        }
        Object endpointPath = attributes.get("endpointPath");
        if (endpointPath == null) {
            return true;
        }
        List<String> allowed = ENDPOINT_DEST_WHITELIST.get(endpointPath.toString());
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        for (String prefix : allowed) {
            if (destination.startsWith(prefix) || destination.startsWith("/user" + prefix)) {
                return true;
            }
        }
        return false;
    }

    private Integer resolveUserId(Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)) {
            return null;
        }
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof UserDetails userDetails) {
            return Integer.parseInt(userDetails.getUsername());
        }
        return null;
    }

    private Integer resolveConversationId(String destination) {
        Matcher matcher = CONVERSATION_DESTINATION.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }
        String directMatch = matcher.group(1);
        String userMatch = matcher.group(2);
        String value = directMatch != null ? directMatch : userMatch;
        return value == null ? null : Integer.parseInt(value);
    }

}
