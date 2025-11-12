package com.ayor.service.impl;

import com.ayor.aspect.chat.ChatNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.ayor.type.NotificationType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements ConversationMessageService {

    private final AccountMapper accountMapper;

    private final ConversationMapper conversationMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;


    @Override
    @ChatNotif(conversationId = "#conversationMessage.conversationId",
            type = NotificationType.SEND_MSG,
            userId = "#accountId")
    public String sendMessage(ConversationMessageDTO conversationMessage, Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        if(account == null) {
            return "用户不存在";
        }
        if(!conversationMapper.existsConversationById(conversationMessage.getConversationId())) {
            return "会话不存在";
        }
        ConversationMessage message = ConversationMessage.builder()
                .conversationId(conversationMessage.getConversationId())
                .accountId(account.getAccountId())
                .content(conversationMessage.getContent())
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        if (this.save(message)) {
            ConversationMessageVO messageVO = new ConversationMessageVO();
            BeanUtils.copyProperties(message, messageVO);
            messageVO.setAvatarUrl(account.getAvatarUrl());
            Integer chatPartnerId = conversationMapper.getChatPartnerId(account.getAccountId(), conversationMessage
                                                        .getConversationId());
            simpMessagingTemplate
                    .convertAndSendToUser(chatPartnerId.toString(),
                            "/transfer/conversation/" + conversationMessage.getConversationId(),
                            messageVO);
            simpMessagingTemplate
                    .convertAndSendToUser(accountId.toString(),
                            "/transfer/conversation/" + conversationMessage.getConversationId(),
                            messageVO);
            return null;
        }
        return "发送失败";

    }


    @Override
    @ChatNotif(conversationId = "#conversationId",
            type = NotificationType.RECEIVED_MSG, userId = "#accountId")
    public PageEntity<ConversationMessageVO> getConversationMessageList(Integer conversationId, Integer accountId, Integer pageNum) {
        Page<ConversationMessage> page = this.lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .orderByDesc(ConversationMessage::getCreateTime)
                .page(Page.of(pageNum, 20));
        List<ConversationMessageVO> conversationMessageVOS = new ArrayList<>();
        page.getRecords().forEach(message -> {
            ConversationMessageVO conversationMessageVO = new ConversationMessageVO();
            BeanUtils.copyProperties(message, conversationMessageVO);
            conversationMessageVO
                    .setAvatarUrl(accountMapper.getAvatarUrlById(message.getAccountId()));
            conversationMessageVOS.add(conversationMessageVO);
        });

        return new PageEntity<>(page.getTotal(), conversationMessageVOS);
    }

}
