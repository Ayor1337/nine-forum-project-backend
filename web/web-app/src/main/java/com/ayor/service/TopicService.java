package com.ayor.service;

import com.ayor.entity.app.dto.TopicDTO;
import com.ayor.entity.app.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TopicService extends IService<Topic> {
    String getTopicNameById(Integer topicId);

    List<TopicVO> getTopicListByThemeId(Integer themeId);

    String insertTopic(TopicDTO topicDTO);

    String updateTopic(TopicDTO topicDTO);

    String deleteTopic(Integer topicId);
}
