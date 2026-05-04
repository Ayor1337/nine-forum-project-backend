package com.ayor.service.impl;

import com.ayor.service.DataRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DataRepairServiceImpl implements DataRepairService {

    private static final String INSERT_MISSING_ACCOUNT_STATS = """
            INSERT INTO account_stat (
                account_id,
                thread_count,
                post_count,
                reply_count,
                liked_count,
                collected_count,
                following_count,
                follower_count
            )
            SELECT
                account.account_id,
                0,
                0,
                0,
                0,
                0,
                0,
                0
            FROM account
            LEFT JOIN account_stat ON account_stat.account_id = account.account_id
            WHERE account_stat.account_id IS NULL
            """;

    private static final String INSERT_MISSING_ACCOUNT_INFO = """
            INSERT INTO account_info (
                account_id,
                create_time,
                update_time
            )
            SELECT
                account.account_id,
                NOW(),
                NOW()
            FROM account
            LEFT JOIN account_info ON account_info.account_id = account.account_id
            WHERE account_info.account_id IS NULL
            """;

    private static final String INSERT_MISSING_USER_PRIVACY_SETTINGS = """
            INSERT INTO user_privacy_setting (
                account_id,
                profile_visibility,
                liked_threads_visibility,
                collected_threads_visibility,
                follow_list_visibility,
                follower_list_visibility,
                birthday_visibility,
                dm_permission,
                create_time,
                update_time
            )
            SELECT
                account.account_id,
                'PUBLIC',
                'PUBLIC',
                'PRIVATE',
                'PUBLIC',
                'PUBLIC',
                'PRIVATE',
                'EVERYONE',
                NOW(),
                NOW()
            FROM account
            LEFT JOIN user_privacy_setting ON user_privacy_setting.account_id = account.account_id
            WHERE user_privacy_setting.account_id IS NULL
            """;

    private static final String INSERT_MISSING_TOPIC_STATS = """
            INSERT INTO topic_stat (
                topic_id,
                thread_count,
                view_count
            )
            SELECT
                topic.topic_id,
                0,
                0
            FROM topic
            LEFT JOIN topic_stat ON topic_stat.topic_id = topic.topic_id
            WHERE topic_stat.topic_id IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    private final CacheManager cacheManager;

    @Override
    public String initializeMissingRelatedRecords() {
        jdbcTemplate.update(INSERT_MISSING_ACCOUNT_STATS);
        jdbcTemplate.update(INSERT_MISSING_ACCOUNT_INFO);
        jdbcTemplate.update(INSERT_MISSING_USER_PRIVACY_SETTINGS);
        jdbcTemplate.update(INSERT_MISSING_TOPIC_STATS);
        clearCache("topicList");
        clearCache("themeTopicList");
        clearCache("themeList");
        clearCache("userPrivacySetting");
        clearCache("userInfo");
        return null;
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
