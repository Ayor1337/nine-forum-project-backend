package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TopicStatService extends IService<TopicStat>  {

    PageEntity<TopicStat> getTopicStats(Integer pageNum, Integer pageSize, Integer topicId);

    String updateTopicStat(Integer statId, TopicStat topicStat);
}
