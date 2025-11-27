package com.ayor.service;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationService extends IService<Conversation> {

    ConversationVO getConversationByAccountId(Integer accountId, Integer toAccountId);

    String hiddenConversation(Integer conversationId, Integer accountId);

    String createNewConversation(Integer accountId, String toUsername);

    List<ConversationVO> getConversationList(Integer accountId);

    List<ChatUnread> getUnreadList(Integer accountId);

    String clearUnread(Integer conversationId, Integer fromUserId);
}
