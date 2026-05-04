package com.ayor.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataRepairServiceImplTest {

    @Test
    void shouldExecuteAllRepairStatements() {
        List<String> executedSql = new ArrayList<>();
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "topicList",
                "themeTopicList",
                "themeList",
                "userPrivacySetting",
                "userInfo"
        );
        cacheManager.getCache("topicList").put("all", "stale");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");
        cacheManager.getCache("userPrivacySetting").put(1, "stale");
        cacheManager.getCache("userInfo").put(1, "stale");
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public int update(String sql) {
                executedSql.add(sql);
                return 1;
            }
        };
        DataRepairServiceImpl service = new DataRepairServiceImpl(jdbcTemplate, cacheManager);

        assertNull(service.initializeMissingRelatedRecords());
        assertEquals(4, executedSql.size());
        assertEquals(true, executedSql.get(0).contains("INSERT INTO account_stat"));
        assertEquals(true, executedSql.get(1).contains("INSERT INTO account_info"));
        assertEquals(true, executedSql.get(2).contains("INSERT INTO user_privacy_setting"));
        assertEquals(true, executedSql.get(3).contains("INSERT INTO topic_stat"));
        assertNull(cacheManager.getCache("topicList").get("all"));
        assertNull(cacheManager.getCache("themeTopicList").get("all"));
        assertNull(cacheManager.getCache("themeList").get("all"));
        assertNull(cacheManager.getCache("userPrivacySetting").get(1));
        assertNull(cacheManager.getCache("userInfo").get(1));
    }
}
