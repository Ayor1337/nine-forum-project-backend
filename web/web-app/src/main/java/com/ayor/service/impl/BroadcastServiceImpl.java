package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.app.vo.SystemMessageVO;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.entity.pojo.SystemMessage;
import com.ayor.mapper.SystemMessageMapper;
import com.ayor.service.BroadcastService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BroadcastServiceImpl implements BroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final SystemMessageMapper systemMessageMapper;

    @Override
    @MessageUnreadNotif(accountId = "#message.sendTo", subscribeDest = "/notif/system", type = UnreadMessageType.SYSTEM_MESSAGE)
    public <T> void userSystemBroadcast(UserSystemMessage<T> message) {

        SystemMessage systemMessage = SystemMessage
                .SystemMessageIndividual(message.getTitle(),
                String.valueOf(message.getMessage()),
                message.getSendTo());
        systemMessageMapper.insert(systemMessage);

        SystemMessageVO messageVO = SystemMessageVO.builder()
                .systemMessageId(systemMessage.getSystemMessageId())
                .title(message.getTitle())
                .content((String) message.getMessage())
                .createTime(systemMessage.getCreateTime())
                .build();

        if (stompUtils.isUserSubscribed(message.getSendTo().toString(), "/notif/system")) {
            messagingTemplate.convertAndSendToUser(message.getSendTo().toString(), "/notif/system", messageVO);
        }
    }

    @Override
    @MessageUnreadNotif(accountId = "#message.sendTo", subscribeDest = "/notif/system", type = UnreadMessageType.SYSTEM_MESSAGE)
    public <T> void userViolationBroadcast(UserViolationMessage<T> message) {
        SystemMessage systemMessage = SystemMessage
                .SystemMessageIndividual(message.getTitle(),
                        String.valueOf(message.getMessage()),
                        message.getSendTo());
        systemMessageMapper.insert(systemMessage);

        SystemMessageVO messageVO = SystemMessageVO.builder()
                .systemMessageId(systemMessage.getSystemMessageId())
                .title(message.getTitle())
                .content((String) message.getMessage())
                .createTime(systemMessage.getCreateTime())
                .build();

        if (stompUtils.isUserSubscribed(message.getSendTo().toString(), "/notif/system")) {
            messagingTemplate.convertAndSendToUser(message.getSendTo().toString(), "/notif/system", messageVO);
        }

    }

}
