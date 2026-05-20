package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.entity.vo.TopicStatVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 分类统计管理服务接口(管理员版)
 *
 * 提供后台分类统计数据管理功能,包括统计查询和更新等管理员专用操作。
 *
 * 主要功能:
 * - 统计查询: 按分类分页查询统计数据
 * - 统计管理: 更新分类统计数据
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see TopicStat 分类统计实体
 * @author ayor
 * @since 1.0.0
 */
public interface TopicStatService extends IService<TopicStat>  {

    /**
     * 获取分类统计数据列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param topicId 分类ID过滤条件;为null时查询所有分类
     * @return 分页结果,包含分类统计数据
     */
    PageEntity<TopicStatVO> getTopicStats(Integer pageNum, Integer pageSize, Integer topicId);

    /**
     * 获取单条话题统计记录
     * @param statId 统计记录ID
     * @return 话题统计记录
     */
    TopicStatVO getTopicStatById(Integer statId);

    /**
     * 创建话题统计记录
     * @param topicStat 话题统计记录
     * @return 操作结果消息
     */
    String createTopicStat(TopicStat topicStat);

    /**
     * 更新分类统计数据
     * @param statId 统计记录ID
     * @param topicStat 分类统计实体对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateTopicStat(Integer statId, TopicStat topicStat);

    /**
     * 删除话题统计记录
     * @param statId 统计记录ID
     * @return 操作结果消息
     */
    String deleteTopicStat(Integer statId);
}
