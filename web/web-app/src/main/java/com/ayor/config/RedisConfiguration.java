package com.ayor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory defaultFactory) {
        return new StringRedisTemplate(defaultFactory);
    }

    @Bean("cacheRedisConnectionFactory")
    public RedisConnectionFactory cacheRedisConnectionFactory(RedisProperties props) {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(props.getHost(), props.getPort());
        config.setDatabase(1);
        if (props.getPassword() != null) {
            config.setPassword(RedisPassword.of(props.getPassword()));
        }

        return new LettuceConnectionFactory(config);
    }


    @Bean
    @Primary
    public RedisCacheManager cacheManager(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory cf1) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        return RedisCacheManager.builder(cf1)
                .cacheDefaults(config)
                .build();
    }

}
