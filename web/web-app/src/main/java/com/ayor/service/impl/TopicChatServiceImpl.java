package com.ayor.service.impl;

import com.ayor.entity.pojo.TopicChat;
import com.ayor.mapper.TopicChatMapper;
import com.ayor.service.TopicChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TopicChatServiceImpl extends ServiceImpl<TopicChatMapper, TopicChat> implements TopicChatService {



}
