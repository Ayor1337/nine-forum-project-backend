package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.document.ThreadDoc;
import com.ayor.entity.app.vo.HotKeywordVO;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 搜索服务接口
 *
 * 提供基于Elasticsearch的全文搜索功能,支持多条件过滤、排序和搜索历史管理。
 *
 * 主要功能:
 * - 全文搜索: 关键词搜索帖子和评论
 * - 高级过滤: 按时间范围、分类、作者等过滤
 * - 搜索历史: 记录和管理用户搜索历史
 * - 热门关键词: 统计热门搜索词
 *
 * 技术特性:
 * - 使用Elasticsearch作为搜索引擎
 * - 支持按相关性、时间排序
 * - Redis存储搜索历史
 *
 * @see ThreadDoc Elasticsearch文档结构
 * @see HotKeywordVO 热门关键词视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface SearchService {

    /**
     * 搜索论坛帖子(支持多条件过滤和排序)
     *
     * 使用Elasticsearch进行全文搜索,支持按时间范围、分类、相关性等多维度过滤和排序。
     * 可选择是否记录搜索历史,用于个性化推荐和热词统计。
     *
     * @param keyword 搜索关键词,必填,支持中文分词和模糊匹配
     * @param userId 当前用户ID,用于记录搜索历史;为null时不记录
     * @param topicId 分类ID过滤条件;为null时搜索所有分类
     * @param enableHistory 是否记录搜索历史(会存入Redis)
     * @param onlyThreadTopic 搜索范围: true=仅搜索帖子标题和内容,false=包含评论内容
     * @param startTime 搜索时间范围起点(毫秒时间戳);为null时不限制起始时间
     * @param endTime 搜索时间范围终点(毫秒时间戳);为null时不限制结束时间
     * @param order 排序方式:
     *              - "asc" = 按创建时间升序
     *              - "desc" = 按创建时间降序
     *              - "rel" = 按相关性评分排序(默认推荐)
     * @param pageNum 分页页码,从1开始
     * @param pageSize 每页记录数,建议10-50
     *
     * @return 分页结果,包含匹配的ThreadDoc列表和总记录数
     *
     * @see ThreadDoc#getScore() 相关性评分
     * @note 搜索历史会异步存入Redis,Key格式为 "search:history:{userId}"
     */
    PageEntity<ThreadDoc> searchThreads(String keyword,
                                        Integer userId,
                                        Integer topicId,
                                        boolean enableHistory,
                                        boolean onlyThreadTopic,
                                        Long startTime,
                                        Long endTime,
                                        String order,
                                        int pageNum,
                                        int pageSize);

    /**
     * 获取用户搜索历史
     * @param userId 用户ID
     * @return 搜索关键词集合,按时间倒序排列
     */
    Set<String> getSearchHistory(Integer userId);

    /**
     * 删除指定关键词的搜索历史
     * @param keyword 要删除的关键词
     * @param userId 用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeSearchHistory(String keyword, Integer userId);

    /**
     * 清空用户所有搜索历史
     * @param userId 用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeSearchHistory(Integer userId);

    /**
     * 获取热门搜索关键词
     * @param size 返回的关键词数量
     * @param duration 统计时间范围,例如Duration.ofDays(7)表示最近7天
     * @return 热门关键词视图对象列表,按搜索次数降序排列
     */
    List<HotKeywordVO> getHotKeywords(int size, Duration duration);
}
