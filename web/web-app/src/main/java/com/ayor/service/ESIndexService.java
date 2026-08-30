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
     * 全量重建 Elasticsearch 索引：保障索引结构、灌入全部有效数据并清理失效文档。
     * @note 应用启动时自动执行, 也可由后台重建命令触发
     */
    void initIndex();
}
