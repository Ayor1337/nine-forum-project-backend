package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TopicService extends IService<Topic> {

    PageEntity<TopicVO> getTopics(Integer pageNum, Integer pageSize);

    PageEntity<TopicVO> getTopicsByThemeId(Integer themeId, Integer pageNum, Integer pageSize);
}
