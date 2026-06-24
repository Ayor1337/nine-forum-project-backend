package com.ayor.service.impl;

import com.ayor.config.WebAuthnProperties;
import com.ayor.service.PasskeyRequestStore;
import com.ayor.util.CONST;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RedisPasskeyRequestStore implements PasskeyRequestStore {

    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); " +
                    "if value then redis.call('DEL', KEYS[1]); end; " +
                    "return value;",
            String.class
    );

    private final StringRedisTemplate redisTemplate;

    private final WebAuthnProperties webAuthnProperties;

    private final ObjectMapper objectMapper;

    public RedisPasskeyRequestStore(StringRedisTemplate redisTemplate,
                                    WebAuthnProperties webAuthnProperties,
                                    ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.webAuthnProperties = webAuthnProperties;
        this.objectMapper = objectMapper.copy().findAndRegisterModules();
    }

    @Override
    public void save(ChallengeSnapshot snapshot) {
        redisTemplate.opsForValue().set(key(snapshot.requestId()), serialize(snapshot), webAuthnProperties.getChallengeExpireSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public ChallengeSnapshot consume(String requestId) {
        ChallengeSnapshot snapshot = deserialize(redisTemplate.execute(CONSUME_SCRIPT, java.util.List.of(key(requestId))));
        if (snapshot == null || snapshot.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return snapshot;
    }

    private String key(String requestId) {
        return CONST.PASSKEY_CHALLENGE + requestId;
    }

    private String serialize(ChallengeSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Passkey challenge 序列化失败", ex);
        }
    }

    private ChallengeSnapshot deserialize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, ChallengeSnapshot.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Passkey challenge 反序列化失败", ex);
        }
    }
}
