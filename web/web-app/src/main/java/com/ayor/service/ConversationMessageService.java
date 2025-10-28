package com.ayor.service;

import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.pojo.ConversationMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationMessageService extends IService<ConversationMessage> {
    String sendMessage(ConversationMessageDTO conversationMessage, String username);

    List<ConversationMessageVO> getConversationMessageList(Integer conversationId);
}
