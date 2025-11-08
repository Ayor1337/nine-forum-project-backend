package com.ayor.service.impl;


import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements ConversationMessageService {


}
