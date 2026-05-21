package com.ayor.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisConfigurationTest {

    @Test
    void threadRankingCacheShouldUseLongTtl() {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();
        RedisCacheConfiguration cacheConfiguration = RedisConfiguration.cacheConfigurations(defaultCacheConfiguration)
                .get("threadRanking");

        assertEquals(Duration.ofHours(6), cacheConfiguration.getTtl());
        assertTrue(RedisConfiguration.cacheConfigurations(defaultCacheConfiguration).containsKey("threadRanking"));
    }
}
