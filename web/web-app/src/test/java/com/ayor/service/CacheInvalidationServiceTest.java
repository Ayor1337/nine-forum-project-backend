package com.ayor.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheInvalidationServiceTest {

    @AfterEach
    void cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    // 测试事务提交后清理缓存
    @Test
    void shouldClearCacheAfterTransactionCommit() {
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("threadRanking")).thenReturn(cache);
        CacheInvalidationService service = new CacheInvalidationService(cacheManager);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        service.clearThreadRanking();

        verify(cache, never()).clear();
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(cache).clear();
    }
}
