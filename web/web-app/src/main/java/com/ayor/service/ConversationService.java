package com.ayor.service;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationService extends IService<Conversation> {
    String createNewConversation(String username, String toUsername);

    List<ConversationVO> getConversationList(String username);
}
