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
import org.springframework.util.StringUtils;

import java.security.Principal;
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

    private static final Pattern CONVERSATION_SUBSCRIPTION_DESTINATION =
            Pattern.compile("^/user/transfer/conversation/(\\d+)(?:/typing)?$");

    private static final Pattern TYPING_SEND_DESTINATION =
            Pattern.compile("^/app/conversations/(\\d+)/typing$");

    private static final Pattern VERIFY_DESTINATION = Pattern.compile("^/verify/[^/]+$");

    private static final String CHATBOARD_ENDPOINT = "/chatboard";
    private static final String CHAT_ENDPOINT = "/chat";
    private static final String SYSTEM_ENDPOINT = "/system";
    private static final String FORUM_ENDPOINT = "/forum";

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
                if (!StringUtils.hasText(authorization)) {
                    return message;
                }
                DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
                if (jwt == null) {
                    throw new AccessDeniedException("无效的连接令牌");
                }
                UserDetails user = jwtUtil.toUser(jwt);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                acc.setUser(authentication);
                if (acc.getSessionAttributes() != null) {
                    acc.getSessionAttributes().put("accountId", user.getUsername());
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
        String endpointPath = endpointPath(accessor);
        if (destination == null || endpointPath == null) {
            return false;
        }
        if (CHATBOARD_ENDPOINT.equals(endpointPath)) {
            return isDestinationUnder(destination, "/broadcast");
        }
        if (FORUM_ENDPOINT.equals(endpointPath)) {
            return isDestinationUnder(destination, "/broadcast");
        }
        if (SYSTEM_ENDPOINT.equals(endpointPath)) {
            return VERIFY_DESTINATION.matcher(destination).matches()
                    || (isAuthenticated(p) && isUserDestinationUnder(destination, "/notif"));
        }
        if (CHAT_ENDPOINT.equals(endpointPath)) {
            if (isUserDestinationUnder(destination, "/notif")) {
                return isAuthenticated(p);
            }
            if (isUserDestinationUnder(destination, "/transfer")) {
                Integer userId = resolveUserId(p);
                if (userId == null) {
                    return false;
                }
                Integer conversationId = resolveConversationId(destination, CONVERSATION_SUBSCRIPTION_DESTINATION);
                if (conversationId == null) {
                    return false;
                }
                authorizationService.assertCanAccessConversation(userId, conversationId);
                return true;
            }
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
        if (!CHAT_ENDPOINT.equals(endpointPath(accessor)) || destination == null) {
            return false;
        }
        Integer userId = resolveUserId(p);
        if (userId == null) {
            return false;
        }
        Integer conversationId = resolveConversationId(destination, TYPING_SEND_DESTINATION);
        if (conversationId == null) {
            return false;
        }
        authorizationService.assertCanAccessConversation(userId, conversationId);
        return true;
    }

    private boolean isAuthenticated(Principal principal) {
        return resolveUserId(principal) != null;
    }

    private boolean isDestinationUnder(String destination, String prefix) {
        return destination.equals(prefix) || destination.startsWith(prefix + "/");
    }

    private boolean isUserDestinationUnder(String destination, String prefix) {
        return destination.equals("/user" + prefix) || destination.startsWith("/user" + prefix + "/");
    }

    private String endpointPath(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            return null;
        }
        Object endpointPath = attributes.get("endpointPath");
        if (!(endpointPath instanceof String path)) {
            return null;
        }
        return switch (path) {
            case CHATBOARD_ENDPOINT, CHAT_ENDPOINT, SYSTEM_ENDPOINT, FORUM_ENDPOINT -> path;
            default -> null;
        };
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

    private Integer resolveConversationId(String destination, Pattern destinationPattern) {
        Matcher matcher = destinationPattern.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

}
