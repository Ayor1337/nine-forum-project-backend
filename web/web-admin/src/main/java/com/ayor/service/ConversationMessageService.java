package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ConversationMessageService extends IService<ConversationMessage> {

    PageEntity<ConversationMessage> getMessages(Integer conversationId, Integer pageNum, Integer pageSize);

    String deleteMessage(Integer messageId);
}
