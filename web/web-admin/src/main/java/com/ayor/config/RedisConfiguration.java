package com.ayor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 创建默认 Redis 连接工厂，供业务 RedisTemplate 使用。
     */
    @Bean("defaultRedisConnectionFactory")
    public RedisConnectionFactory defaultRedisConnectionFactory (RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setDatabase(0);
        if (redisProperties.getHost() != null) {
            config.setHostName(redisProperties.getHost());
        }
        if (redisProperties.getPort() != 6379) {
            config.setPort(redisProperties.getPort());
        }
        if (redisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * 创建字符串类型的 RedisTemplate。
     */
    @Bean("redisTemplate")
    public StringRedisTemplate stringRedisTemplate (@Qualifier("defaultRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * 创建缓存专用 Redis 连接工厂，使用独立数据库避免和业务数据互相影响。
     */
    @Bean("cacheRedisConnectionFactory")
    public RedisConnectionFactory cacheRedisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setDatabase(1);
        if (redisProperties.getHost() != null) {
            config.setHostName(redisProperties.getHost());
        }
        if (redisProperties.getPort() != 6379) {
            config.setPort(redisProperties.getPort());
        }
        if (redisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * 创建 RedisCacheManager，供 Spring Cache 使用。
     */
    @Bean
    public RedisCacheManager cacheManager(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .build();

    }


}
