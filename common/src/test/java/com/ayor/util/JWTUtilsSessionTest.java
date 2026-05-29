package com.ayor.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JWTUtilsSessionTest {

    private JWTUtils jwtUtils;
    private StringRedisTemplate redisTemplate;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtUtils = new JWTUtils();
        redisTemplate = mock(StringRedisTemplate.class);
        ReflectionTestUtils.setField(jwtUtils, "template", redisTemplate);
        ReflectionTestUtils.setField(jwtUtils, "key", "test-secret");
        ReflectionTestUtils.setField(jwtUtils, "expire", 3600);
        user = User.withUsername("tester").password("N/A").roles("USER").build();
    }

    @Test
    void shouldResolveJwtWhenSessionIsActive() {
        JWTUtils.LoginJwt loginJwt = jwtUtils.createLoginJwt(user, 7, "tester", "session-1");
        when(redisTemplate.hasKey(argThat(key -> key != null && key.startsWith(CONST.JWT_BLACK_LIST)))).thenReturn(false);
        when(redisTemplate.hasKey(CONST.LOGIN_SESSION_ACTIVE + "session-1")).thenReturn(true);

        DecodedJWT decodedJWT = jwtUtils.resolveJwt("Bearer " + loginJwt.token());

        assertNotNull(decodedJWT);
        assertEquals("session-1", decodedJWT.getClaim("sid").asString());
        assertEquals(loginJwt.jwtId(), decodedJWT.getId());
    }

    @Test
    void shouldRejectJwtWhenSessionIsMissing() {
        JWTUtils.LoginJwt loginJwt = jwtUtils.createLoginJwt(user, 7, "tester", "session-1");
        when(redisTemplate.hasKey(argThat(key -> key != null && key.startsWith(CONST.JWT_BLACK_LIST)))).thenReturn(false);
        when(redisTemplate.hasKey(CONST.LOGIN_SESSION_ACTIVE + "session-1")).thenReturn(false);

        DecodedJWT decodedJWT = jwtUtils.resolveJwt("Bearer " + loginJwt.token());

        assertNull(decodedJWT);
    }
}
