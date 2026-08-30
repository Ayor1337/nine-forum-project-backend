package com.ayor.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisConfigurationTest {

    // 测试缓存使用显式 TTL
    @Test
    void cachesShouldUseExplicitTtl() {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();
        var configurations = RedisConfiguration.cacheConfigurations(defaultCacheConfiguration);

        assertEquals(Duration.ofMinutes(30), defaultCacheConfiguration.getTtl());
        assertEquals(Duration.ofMinutes(15), configurations.get("userPrivacySetting").getTtl());
        assertEquals(Duration.ofMinutes(15), configurations.get("userRelationBlocked").getTtl());
        assertEquals(Duration.ofMinutes(15), configurations.get("conversation").getTtl());
        assertEquals(Duration.ofMinutes(30), configurations.get("userInfo").getTtl());
        assertEquals(Duration.ofHours(1), configurations.get("themeList").getTtl());
        assertEquals(Duration.ofHours(1), configurations.get("topicList").getTtl());
        assertEquals(Duration.ofMinutes(30), configurations.get("threadRanking").getTtl());
        assertTrue(configurations.containsKey("threadRanking"));
    }
}
