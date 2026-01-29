package com.ayor.service;

/**
 * Elasticsearch索引服务接口
 *
 * 提供Elasticsearch索引的初始化功能。
 *
 * 主要功能:
 * - 索引初始化: 创建和初始化Elasticsearch索引结构
 *
 * @author ayor
 * @since 1.0.0
 */
public interface ESIndexService {

    /**
     * 初始化Elasticsearch索引
     * @note 通常在应用启动时调用,创建必要的索引和映射
     */
    void initIndex();
}
