package securitycontract;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.config.CorsProperties;
import com.ayor.config.WebsocketConfiguration;
import com.ayor.interceptor.StompAuthInterceptor;
import com.ayor.mapper.RoleMapper;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@ContextConfiguration(classes = AdminStompSecurityContractTest.TestConfiguration.class)
class AdminStompSecurityContractTest {

    private static final String OWNER_AUTHORIZATION = "Bearer owner";
    private static final String USER_AUTHORIZATION = "Bearer user";

    @jakarta.annotation.Resource
    private MessageChannel clientInboundChannel;

    @MockitoBean
    private JWTUtils jwtUtils;

    @MockitoBean
    private RoleMapper roleMapper;

    @Test
    void clientInboundChannelRejectsRegularUserConnect() {
        DecodedJWT jwt = jwt("member");
        when(jwtUtils.resolveJwt(USER_AUTHORIZATION)).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(user("42"));
        when(roleMapper.getRoleNameByUsername("member")).thenReturn("USER");

        Message<?> connect = connect(USER_AUTHORIZATION);
        MessageDeliveryException exception = assertThrows(MessageDeliveryException.class,
                () -> clientInboundChannel.send(connect));

        assertInstanceOf(AccessDeniedException.class, exception.getCause());
    }

    @Test
    void clientInboundChannelAllowsOwnerConnectAndReportsSubscription() {
        DecodedJWT jwt = jwt("owner");
        when(jwtUtils.resolveJwt(OWNER_AUTHORIZATION)).thenReturn(jwt);
        when(jwtUtils.toUser(jwt)).thenReturn(user("7"));
        when(roleMapper.getRoleNameByUsername("owner")).thenReturn("OWNER");

        Message<?> connect = connect(OWNER_AUTHORIZATION);
        assertDoesNotThrow(() -> clientInboundChannel.send(connect));
        StompHeaderAccessor connectedAccessor = StompHeaderAccessor.wrap(connect);
        assertNotNull(connectedAccessor.getUser());

        Message<?> subscribe = subscribe(connectedAccessor.getUser(),
                connectedAccessor.getSessionAttributes(), connectedAccessor.getSessionId());
        assertDoesNotThrow(() -> clientInboundChannel.send(subscribe));
    }

    private Message<?> connect(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", authorization);
        accessor.setSessionId("session-" + authorization.substring("Bearer ".length()));
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> subscribe(java.security.Principal principal,
                                 Map<String, Object> sessionAttributes,
                                 String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSubscriptionId("reports-subscription");
        accessor.setSessionId(sessionId);
        accessor.setDestination("/topic/reports");
        accessor.setUser(principal);
        accessor.setSessionAttributes(sessionAttributes);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private DecodedJWT jwt(String username) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(claim.asString()).thenReturn(username);
        when(jwt.getClaim("name")).thenReturn(claim);
        return jwt;
    }

    private UserDetails user(String username) {
        return User.withUsername(username).password("n/a").authorities("ROLE_USER").build();
    }

    @Configuration
    @Import(WebsocketConfiguration.class)
    static class TestConfiguration {

        @Bean
        StompAuthInterceptor stompAuthInterceptor(JWTUtils jwtUtils, RoleMapper roleMapper) {
            return new StompAuthInterceptor(jwtUtils, roleMapper);
        }

        @Bean
        CorsProperties corsProperties() {
            CorsProperties properties = new CorsProperties();
            properties.setAllowedOrigins(List.of("http://localhost:10072"));
            return properties;
        }
    }
}
