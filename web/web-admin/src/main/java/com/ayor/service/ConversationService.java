package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ConversationService extends IService<Conversation> {

    PageEntity<Conversation> getConversations(Integer pageNum, Integer pageSize, Integer alphaAccountId, Integer betaAccountId);

    String deleteConversation(Integer conversationId);
}
