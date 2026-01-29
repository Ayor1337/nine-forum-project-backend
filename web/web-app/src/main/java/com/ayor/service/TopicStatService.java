package com.ayor.service;

import com.ayor.entity.pojo.TopicStat;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 分类统计服务接口
 *
 * 提供分类统计数据的更新功能。
 *
 * 主要功能:
 * - 统计更新: 定时更新分类统计数据(帖子数、评论数等)
 *
 * @see TopicStat 分类统计实体
 * @author ayor
 * @since 1.0.0
 */
public interface TopicStatService extends IService<TopicStat>  {

    /**
     * 更新所有分类的统计数据(定时任务调用)
     * @note 此方法由定时任务触发,不应手动调用
     */
    void updateTopicStat();
}
