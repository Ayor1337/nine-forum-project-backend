package com.ayor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 帖子榜单缓存名称。
     *
     * 榜单允许延迟更新，使用独立缓存名便于单独配置 TTL 与后续按缓存名清理。
     */
    private static final String THREAD_RANKING_CACHE = "threadRanking";

    /**
     * 帖子榜单缓存过期时间。
     *
     * 当前榜单不要求实时刷新，6 小时 TTL 可以减少重复排序查询，同时避免长期展示旧数据。
     */
    private static final Duration THREAD_RANKING_CACHE_TTL = Duration.ofHours(6);

    /**
     * 创建默认 Redis 连接工厂。
     *
     * 默认 Redis 使用 DB 0，供业务层直接读写字符串数据，例如验证码、临时状态等。
     *
     * @param redisProperties Redis 配置
     * @return 连接工厂
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
     * 创建字符串 Redis 模板。
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 字符串模板
     */
    @Bean("redisTemplate")
    public StringRedisTemplate stringRedisTemplate (@Qualifier("defaultRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * 创建缓存 Redis 连接工厂。
     *
     * Spring Cache 使用独立的 DB 1，避免缓存数据与业务直接写入的 Redis key 混在一起。
     *
     * @param redisProperties Redis 配置
     * @return 连接工厂
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
     * 创建 Redis 缓存管理器。
     *
     * 默认不缓存 null 值；需要特殊 TTL 的缓存通过 {@link #cacheConfigurations(RedisCacheConfiguration)}
     * 单独声明，避免影响其它缓存的生命周期。
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations(defaultCacheConfiguration))
                .build();

    }

    /**
     * 构造需要特殊配置的缓存集合。
     *
     * 目前仅帖子榜单缓存使用 6 小时 TTL。后续若有其它缓存需要不同过期时间，
     * 可以在这里按缓存名追加配置。
     *
     * @param defaultCacheConfiguration 默认缓存配置
     * @return 按缓存名区分的 Redis 缓存配置
     */
    static Map<String, RedisCacheConfiguration> cacheConfigurations(RedisCacheConfiguration defaultCacheConfiguration) {
        return Map.of(
                THREAD_RANKING_CACHE,
                defaultCacheConfiguration.entryTtl(THREAD_RANKING_CACHE_TTL)
        );
    }

}
