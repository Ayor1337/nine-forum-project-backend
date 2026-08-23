package com.ayor.service.impl;

import com.ayor.config.RegistrationVerificationProperties;
import com.ayor.service.RegistrationVerificationGate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在显式启用时针对独立 Redis 逻辑 DB 执行 Lua 边界测试。
 * 使用 {@code -Dnineforum.redis.it.enabled=true} 启用；host、port、database 和 password
 * 可通过同名前缀的系统属性覆盖，默认连接 {@code 127.0.0.1:16379/15}。
 */
@Execution(ExecutionMode.SAME_THREAD)
@EnabledIfSystemProperty(named = "nineforum.redis.it.enabled", matches = "(?i)true")
class RedisRegistrationVerificationGateIntegrationTest {

    private static final String PREFIX = "registration:verify:";
    private static final String GLOBAL_KEY = PREFIX + "global";
    private static final String GLOBAL_ALERT_KEY = PREFIX + "global:alert";

    private final Set<String> createdKeys = new HashSet<>();

    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private boolean ownsEmptyPrefix;

    @BeforeEach
    void setUp() {
        String host = System.getProperty("nineforum.redis.it.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("nineforum.redis.it.port", "16379"));
        int database = Integer.parseInt(System.getProperty("nineforum.redis.it.database", "15"));
        String password = System.getProperty("nineforum.redis.it.password", "");

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        configuration.setDatabase(database);
        if (!password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }
        connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        Set<String> existingKeys = redisTemplate.keys(PREFIX + "*");
        ownsEmptyPrefix = existingKeys != null && existingKeys.isEmpty();
        Assumptions.assumeTrue(
                ownsEmptyPrefix,
                "所选 Redis 逻辑 DB 已存在 registration:verify:*，为避免误删而跳过"
        );
    }

    @AfterEach
    void tearDown() {
        try {
            if (ownsEmptyPrefix && redisTemplate != null && !createdKeys.isEmpty()) {
                redisTemplate.delete(createdKeys);
            }
        } finally {
            if (connectionFactory != null) {
                connectionFactory.destroy();
            }
        }
    }

    @Test
    void emailQuotaAllowsBoundaryAndRejectsNextRequest() {
        RegistrationVerificationProperties properties = properties(2, 100, 100);
        RedisRegistrationVerificationGate gate = gate(properties);
        String email = "sec07-email-boundary@example.com";
        String remoteAddress = "198.51.100.10";
        track(email, remoteAddress);

        RegistrationVerificationGate.Acquisition first = gate.acquire(email, remoteAddress, "email-id-1");
        gate.complete(email, "email-id-1");
        RegistrationVerificationGate.Acquisition second = gate.acquire(email, remoteAddress, "email-id-2");
        gate.complete(email, "email-id-2");
        RegistrationVerificationGate.Acquisition third = gate.acquire(email, remoteAddress, "email-id-3");

        assertThat(first).isEqualTo(RegistrationVerificationGate.Acquisition.granted("email-id-1"));
        assertThat(second).isEqualTo(RegistrationVerificationGate.Acquisition.granted("email-id-2"));
        assertLimitedWithinWindow(third, properties.getEmailWindowSeconds());
    }

    @Test
    void ipQuotaAllowsBoundaryAndRejectsNextRequest() {
        RegistrationVerificationProperties properties = properties(100, 2, 100);
        RedisRegistrationVerificationGate gate = gate(properties);
        String remoteAddress = "198.51.100.20";
        String firstEmail = "sec07-ip-boundary-1@example.com";
        String secondEmail = "sec07-ip-boundary-2@example.com";
        String thirdEmail = "sec07-ip-boundary-3@example.com";
        track(firstEmail, remoteAddress);
        track(secondEmail, remoteAddress);
        track(thirdEmail, remoteAddress);

        RegistrationVerificationGate.Acquisition first = gate.acquire(firstEmail, remoteAddress, "ip-id-1");
        RegistrationVerificationGate.Acquisition second = gate.acquire(secondEmail, remoteAddress, "ip-id-2");
        RegistrationVerificationGate.Acquisition third = gate.acquire(thirdEmail, remoteAddress, "ip-id-3");

        assertThat(first).isEqualTo(RegistrationVerificationGate.Acquisition.granted("ip-id-1"));
        assertThat(second).isEqualTo(RegistrationVerificationGate.Acquisition.granted("ip-id-2"));
        assertLimitedWithinWindow(third, properties.getIpWindowSeconds());
    }

    @Test
    void globalQuotaAllowsBoundaryAndRejectsNextRequest() {
        RegistrationVerificationProperties properties = properties(100, 100, 2);
        RedisRegistrationVerificationGate gate = gate(properties);
        String firstEmail = "sec07-global-boundary-1@example.com";
        String secondEmail = "sec07-global-boundary-2@example.com";
        String thirdEmail = "sec07-global-boundary-3@example.com";
        track(firstEmail, "198.51.100.31");
        track(secondEmail, "198.51.100.32");
        track(thirdEmail, "198.51.100.33");

        RegistrationVerificationGate.Acquisition first = gate.acquire(firstEmail, "198.51.100.31", "global-id-1");
        RegistrationVerificationGate.Acquisition second = gate.acquire(secondEmail, "198.51.100.32", "global-id-2");
        RegistrationVerificationGate.Acquisition third = gate.acquire(thirdEmail, "198.51.100.33", "global-id-3");

        assertThat(first).isEqualTo(RegistrationVerificationGate.Acquisition.granted("global-id-1"));
        assertThat(second).isEqualTo(RegistrationVerificationGate.Acquisition.granted("global-id-2"));
        assertLimitedWithinWindow(third, properties.getGlobalWindowSeconds());
    }

    @Test
    void idempotentRequestReusesJwtWithoutIncrementingAnyQuota() {
        RegistrationVerificationProperties properties = properties(10, 10, 10);
        RedisRegistrationVerificationGate gate = gate(properties);
        String email = "sec07-idempotent@example.com";
        String firstRemoteAddress = "198.51.100.41";
        String secondRemoteAddress = "198.51.100.42";
        track(email, firstRemoteAddress);
        track(email, secondRemoteAddress);

        RegistrationVerificationGate.Acquisition first = gate.acquire(email, firstRemoteAddress, "idem-id-1");
        RegistrationVerificationGate.Acquisition second = gate.acquire(email, secondRemoteAddress, "idem-id-2");

        assertThat(first).isEqualTo(RegistrationVerificationGate.Acquisition.granted("idem-id-1"));
        assertThat(second).isEqualTo(RegistrationVerificationGate.Acquisition.reused("idem-id-1"));
        assertThat(redisTemplate.opsForValue().get(emailKey(email))).isEqualTo("1");
        assertThat(redisTemplate.opsForValue().get(ipKey(firstRemoteAddress))).isEqualTo("1");
        assertThat(redisTemplate.hasKey(ipKey(secondRemoteAddress))).isFalse();
        assertThat(redisTemplate.opsForValue().get(GLOBAL_KEY)).isEqualTo("1");
    }

    private RedisRegistrationVerificationGate gate(RegistrationVerificationProperties properties) {
        return new RedisRegistrationVerificationGate(redisTemplate, properties);
    }

    private RegistrationVerificationProperties properties(int emailLimit, int ipLimit, int globalLimit) {
        RegistrationVerificationProperties properties = new RegistrationVerificationProperties();
        properties.setEmailLimit(emailLimit);
        properties.setEmailWindowSeconds(60);
        properties.setIpLimit(ipLimit);
        properties.setIpWindowSeconds(60);
        properties.setGlobalLimit(globalLimit);
        properties.setGlobalWindowSeconds(60);
        properties.setGlobalAlertPercent(100);
        return properties;
    }

    private void assertLimitedWithinWindow(
            RegistrationVerificationGate.Acquisition acquisition,
            long windowSeconds
    ) {
        assertThat(acquisition.status()).isEqualTo(RegistrationVerificationGate.Status.LIMITED);
        assertThat(acquisition.jwtId()).isNull();
        assertThat(acquisition.retryAfterSeconds()).isBetween(1L, windowSeconds);
    }

    private void track(String email, String remoteAddress) {
        createdKeys.add(idempotencyKey(email));
        createdKeys.add(emailKey(email));
        createdKeys.add(ipKey(remoteAddress));
        createdKeys.add(GLOBAL_KEY);
        createdKeys.add(GLOBAL_ALERT_KEY);
    }

    private String idempotencyKey(String email) {
        return PREFIX + "idem:" + digest(email.trim().toLowerCase(Locale.ROOT));
    }

    private String emailKey(String email) {
        return PREFIX + "email:" + digest(email.trim().toLowerCase(Locale.ROOT));
    }

    private String ipKey(String remoteAddress) {
        return PREFIX + "ip:" + digest(remoteAddress);
    }

    private String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持 SHA-256", exception);
        }
    }
}
