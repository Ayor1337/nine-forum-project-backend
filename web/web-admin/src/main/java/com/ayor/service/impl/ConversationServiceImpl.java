package com.ayor.service.impl;

import com.ayor.entity.pojo.Conversation;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {


}
