package com.ayor.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JWTUtilsEmailTest {

    private JWTUtils jwtUtils;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jwtUtils = new JWTUtils();
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(jwtUtils, "template", redisTemplate);
        ReflectionTestUtils.setField(jwtUtils, "key", "test-secret");
        ReflectionTestUtils.setField(jwtUtils, "expire", 7);
    }

    @Test
    void createEmailJwtUsesRemainingLifetimeAsRedisTtl() {
        jwtUtils.createEmailJwt("user@example.com", "jwt-id");

        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq(CONST.JWT_EMAIL_VERIFY + "jwt-id"),
                org.mockito.ArgumentMatchers.eq(""),
                ttlCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(TimeUnit.MILLISECONDS)
        );
        assertThat(ttlCaptor.getValue()).isBetween(10_790_000L, 10_800_000L);
        assertThat(ttlCaptor.getValue()).isLessThan(System.currentTimeMillis());
    }

    @Test
    void consumeEmailJwtAllowsOnlyTheSuccessfulRedisDeleteClaim() {
        JWTUtils.EmailJwt emailJwt = jwtUtils.createEmailJwt("user@example.com", "jwt-id");
        when(redisTemplate.hasKey(CONST.JWT_EMAIL_VERIFY + "jwt-id")).thenReturn(true);
        when(redisTemplate.delete(CONST.JWT_EMAIL_VERIFY + "jwt-id")).thenReturn(true, false);

        DecodedJWT first = jwtUtils.consumeEmailJwt(emailJwt.token());
        DecodedJWT second = jwtUtils.consumeEmailJwt(emailJwt.token());

        assertThat(first).isNotNull();
        assertThat(first.getId()).isEqualTo("jwt-id");
        assertThat(second).isNull();
    }
}
