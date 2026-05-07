package com.ayor.service.impl;

import com.ayor.config.WebAuthnProperties;
import com.ayor.service.PasskeyRequestStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisPasskeyRequestStoreTest {

    @Test
    void shouldRoundTripRegistrationChallengeSnapshot() {
        FakeRedis fakeRedis = new FakeRedis();
        WebAuthnProperties properties = new WebAuthnProperties();
        RedisPasskeyRequestStore store = new RedisPasskeyRequestStore(fakeRedis.redisTemplate, properties, new ObjectMapper());

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

        PasskeyRequestStore.ChallengeSnapshot restored = store.load("req-1");
        assertNotNull(restored);
        assertEquals(PasskeyRequestStore.RequestType.REGISTRATION, restored.type());
        assertEquals(7, restored.accountId());
        assertEquals("Nw", restored.userHandle());
        verify(fakeRedis.valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldReturnNullWhenSnapshotMissing() {
        RedisPasskeyRequestStore store = new RedisPasskeyRequestStore(mock(StringRedisTemplate.class), new WebAuthnProperties(), new ObjectMapper());

        assertNull(store.load("missing"));
    }

    private static class FakeRedis {
        private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        private final ValueOperations<String, String> valueOperations = mockValueOperations();
        private final Map<String, String> storage = new ConcurrentHashMap<>();

        private FakeRedis() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doAnswer(invocation -> {
                storage.put(invocation.getArgument(0), invocation.getArgument(1));
                return null;
            }).when(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS));
            when(valueOperations.get(anyString())).thenAnswer(invocation -> storage.get(invocation.getArgument(0)));
        }

        @SuppressWarnings("unchecked")
        private static ValueOperations<String, String> mockValueOperations() {
            return mock(ValueOperations.class);
        }
    }
}
