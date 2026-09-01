package com.ayor.config;

import io.lettuce.core.SslVerifyMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisConfigurationTest {

    @Test
    void connectionFactoriesShouldForwardAclUsernameAndPassword() {
        RedisProperties properties = new RedisProperties();
        properties.setUsername("nineforum-app");
        properties.setPassword("test-only-password");

        RedisConfiguration configuration = new RedisConfiguration();
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory)
                configuration.defaultRedisConnectionFactory(properties);

        assertEquals("nineforum-app", connectionFactory.getStandaloneConfiguration().getUsername());
        assertArrayEquals("test-only-password".toCharArray(), connectionFactory.getStandaloneConfiguration().getPassword().get());
        connectionFactory.destroy();

        LettuceConnectionFactory cacheConnectionFactory = (LettuceConnectionFactory)
                configuration.cacheRedisConnectionFactory(properties);
        assertEquals("nineforum-app", cacheConnectionFactory.getStandaloneConfiguration().getUsername());
        cacheConnectionFactory.destroy();
    }

    @Test
    void sslConfigurationShouldEnableFullPeerVerification() {
        RedisProperties properties = new RedisProperties();
        properties.getSsl().setEnabled(true);

        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory)
                new RedisConfiguration().defaultRedisConnectionFactory(properties);

        assertTrue(connectionFactory.getClientConfiguration().isUseSsl());
        assertEquals(SslVerifyMode.FULL, connectionFactory.getClientConfiguration().getVerifyMode());
        connectionFactory.destroy();
    }
}
