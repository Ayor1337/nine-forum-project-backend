package com.ayor.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ayor.mq.EsIndexSyncProducer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataRepairServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private EsIndexSyncProducer esIndexSyncProducer;

    // 测试初始化缺失关联记录执行全部修复 SQL 并清理已知缓存
    @Test
    void initializeMissingRelatedRecordsRunsAllRepairSqlAndClearsKnownCaches() {
        when(cacheManager.getCache(org.mockito.ArgumentMatchers.anyString())).thenReturn(cache);
        DataRepairServiceImpl service = new DataRepairServiceImpl(jdbcTemplate, cacheManager, esIndexSyncProducer);

        String result = service.initializeMissingRelatedRecords();

        assertThat(result).isNull();
        InOrder sqlOrder = inOrder(jdbcTemplate);
        sqlOrder.verify(jdbcTemplate).update(contains("INSERT INTO account_stat"));
        sqlOrder.verify(jdbcTemplate).update(contains("INSERT INTO account_info"));
        sqlOrder.verify(jdbcTemplate).update(contains("INSERT INTO user_privacy_setting"));
        sqlOrder.verify(jdbcTemplate).update(contains("INSERT INTO topic_stat"));
        verify(cacheManager).getCache("topicList");
        verify(cacheManager).getCache("themeTopicList");
        verify(cacheManager).getCache("themeList");
        verify(cacheManager).getCache("userPrivacySetting");
        verify(cacheManager).getCache("userInfo");
        verify(cache, org.mockito.Mockito.times(5)).clear();
    }

    // 测试初始化缺失关联记录忽略缺失缓存
    @Test
    void initializeMissingRelatedRecordsIgnoresMissingCaches() {
        DataRepairServiceImpl service = new DataRepairServiceImpl(jdbcTemplate, cacheManager, esIndexSyncProducer);

        assertThat(service.initializeMissingRelatedRecords()).isNull();

        verify(cache, org.mockito.Mockito.never()).clear();
    }

    // 测试重建搜索索引发送重建命令
    @Test
    void rebuildSearchIndexSendsRebuildCommand() {
        DataRepairServiceImpl service = new DataRepairServiceImpl(jdbcTemplate, cacheManager, esIndexSyncProducer);

        assertThat(service.rebuildSearchIndex()).isNull();

        verify(esIndexSyncProducer).rebuildAll();
    }
}
