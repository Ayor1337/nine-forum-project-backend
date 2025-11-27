package com.ayor.service;

import com.ayor.aspect.chat.ChatNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.type.NotificationType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationMessageService extends IService<ConversationMessage> {

    String sendMessage(ConversationMessageDTO conversationMessage, Integer accountId);

    PageEntity<ConversationMessageVO> getConversationMessageList(Integer conversationId, Integer accountId, Integer pageNum);
}
