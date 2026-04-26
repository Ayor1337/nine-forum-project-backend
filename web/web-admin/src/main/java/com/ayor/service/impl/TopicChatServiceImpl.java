package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.ayor.mapper.TopicChatMapper;
import com.ayor.service.TopicChatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TopicChatServiceImpl extends ServiceImpl<TopicChatMapper, TopicChat> implements TopicChatService {

    @Override
    public PageEntity<TopicChat> getTopicChats(Integer topicId, Integer pageNum, Integer pageSize) {
        Page<TopicChat> page = this.lambdaQuery()
                .eq(topicId != null, TopicChat::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String deleteTopicChat(Integer topicChatId) {
        if (topicChatId == null) {
            return "聊天记录不存在";
        }
        return this.removeById(topicChatId) ? null : "删除聊天记录失败";
    }
}
