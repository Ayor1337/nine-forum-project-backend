package com.ayor.service.impl;

import com.ayor.service.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 缓存失效服务实现
 *
 * 通过事务同步机制将缓存清除动作注册为 afterCommit 回调，
 * 在事务提交成功后执行；当前不在事务中时立即执行。
 */
@Service
@RequiredArgsConstructor
public class CacheInvalidationServiceImpl implements CacheInvalidationService {

    private final CacheManager cacheManager;

    @Override
    public void evict(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        afterCommit(() -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        });
    }

    @Override
    public void clear(String cacheName) {
        afterCommit(() -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Override
    public void clearThreadRanking() {
        clear(THREAD_RANKING_CACHE);
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
