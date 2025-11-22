package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TopicChatService extends IService<TopicChat> {

    PageEntity<TopicChat> getTopicChats(Integer topicId, Integer pageNum, Integer pageSize);

    String deleteTopicChat(Integer topicChatId);
}
