package com.ayor.service.impl;

import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
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
    public String sendMessage(ConversationMessageDTO conversationMessage, String username) {
        Account account = accountMapper.getAccountByUsername(username);
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
            Integer chatPartnerId = conversationMapper.getChatPartnerId(account.getAccountId(), conversationMessage.getConversationId());
            String usernameById = accountMapper.getUsernameById(chatPartnerId);
            simpMessagingTemplate
                    .convertAndSendToUser(usernameById,
                            "/transfer/conversation/" + conversationMessage.getConversationId(),
                            messageVO);
            return null;
        }
        return "发送失败";

    }

    @Override
    public List<ConversationMessageVO> getConversationMessageList(Integer conversationId) {
        List<ConversationMessage> list = this.lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .list();
        List<ConversationMessageVO> conversationMessageVOS = new ArrayList<>();
        list.forEach(message -> {
            ConversationMessageVO conversationMessageVO = new ConversationMessageVO();
            BeanUtils.copyProperties(message, conversationMessageVO);
            conversationMessageVO
                    .setAvatarUrl(accountMapper.getAvatarUrlById(message.getAccountId()));
            conversationMessageVOS.add(conversationMessageVO);
        });

        return conversationMessageVOS;
    }

}
