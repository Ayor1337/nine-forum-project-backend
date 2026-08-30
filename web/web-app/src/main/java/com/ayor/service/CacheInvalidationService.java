package com.ayor.service;

/**
 * 缓存失效服务
 *
 * 业务写操作后统一清除 Spring Cache 缓存。若当前处于事务中，
 * 清除动作会延迟到事务提交成功后执行，避免旧数据被并发请求
 * 重新读入缓存，也避免事务回滚导致的无效清理。
 */
public interface CacheInvalidationService {

    String THREAD_RANKING_CACHE = "threadRanking";

    /**
     * 删除指定缓存项
     *
     * @param cacheName 缓存名称
     * @param key 缓存键，为 null 时不执行任何操作
     */
    void evict(String cacheName, Object key);

    /**
     * 清空指定缓存区
     *
     * @param cacheName 缓存名称
     */
    void clear(String cacheName);

    /**
     * 清空帖子排行缓存（{@value #THREAD_RANKING_CACHE}）
     */
    void clearThreadRanking();
}
