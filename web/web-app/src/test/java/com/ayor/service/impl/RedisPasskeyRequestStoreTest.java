package com.ayor.service.impl;

import com.ayor.config.WebAuthnProperties;
import com.ayor.service.PasskeyRequestStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisPasskeyRequestStoreTest {

    @Test
    void shouldRoundTripRegistrationChallengeSnapshot() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        WebAuthnProperties properties = new WebAuthnProperties();
        RedisPasskeyRequestStore store = new RedisPasskeyRequestStore(redisTemplate, properties, new ObjectMapper());

        PasskeyRequestStore.ChallengeSnapshot snapshot = new PasskeyRequestStore.ChallengeSnapshot(
                "req-1",
                PasskeyRequestStore.RequestType.REGISTRATION,
                "challenge",
                "localhost",
                java.util.List.of("http://localhost:3000"),
                7,
                "Nw",
                Instant.now().plusSeconds(300)
        );

        store.save(snapshot);
        when(redisTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class), any(java.util.List.class)))
                .thenReturn(new ObjectMapper().findAndRegisterModules().writeValueAsString(snapshot));

        PasskeyRequestStore.ChallengeSnapshot restored = store.consume("req-1");
        assertNotNull(restored);
        assertEquals(PasskeyRequestStore.RequestType.REGISTRATION, restored.type());
        assertEquals(7, restored.accountId());
        assertEquals("Nw", restored.userHandle());
        verify(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldReturnNullWhenSnapshotMissing() {
        RedisPasskeyRequestStore store = new RedisPasskeyRequestStore(mock(StringRedisTemplate.class), new WebAuthnProperties(), new ObjectMapper());

        assertNull(store.consume("missing"));
    }

    @Test
    void shouldOnlyReturnChallengeOnce() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        PasskeyRequestStore.ChallengeSnapshot snapshot = new PasskeyRequestStore.ChallengeSnapshot(
                "req-1", PasskeyRequestStore.RequestType.AUTHENTICATION, "challenge", "localhost",
                java.util.List.of("http://localhost:3000"), null, null, Instant.now().plusSeconds(300)
        );
        when(redisTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class), any(java.util.List.class)))
                .thenReturn(new ObjectMapper().findAndRegisterModules().writeValueAsString(snapshot), null);
        RedisPasskeyRequestStore store = new RedisPasskeyRequestStore(redisTemplate, new WebAuthnProperties(), new ObjectMapper());

        assertNotNull(store.consume("req-1"));
        assertNull(store.consume("req-1"));
    }
}
