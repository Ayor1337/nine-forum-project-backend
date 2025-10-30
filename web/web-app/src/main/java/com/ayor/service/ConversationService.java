package com.ayor.service;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationService extends IService<Conversation> {

    ConversationVO getConversationByAccountId(String username, Integer toAccountId);

    String hiddenConversation(Integer conversationId, String username);

    String createNewConversation(String username, String toUsername);

    List<ConversationVO> getConversationList(String username);

    List<ChatUnread> getUnreadList(String username);

    String clearUnread(Integer conversationId, Integer fromUserId);
}
