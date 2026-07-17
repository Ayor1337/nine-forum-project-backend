package com.ayor.util;

import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.pojo.ConversationUserSetting;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.mapper.ConversationUserSettingMapper;
import com.ayor.service.AccountService;
import com.ayor.service.PresenceService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ConversationViewUtils {

    private static final String RECALL_BY_SELF = "你撤回了一条消息";

    private static final String RECALL_BY_PARTNER = "对方撤回了一条消息";

    private static final String LAST_MESSAGE_RECALLED = "消息已撤回";

    private final AccountMapper accountMapper;

    private final AccountService accountService;

    private final ConversationMessageMapper conversationMessageMapper;

    private final ConversationUserSettingMapper conversationUserSettingMapper;

    private final PresenceService presenceService;

    public ConversationMessageVO toMessageVO(ConversationMessage message, Integer viewerId) {
        if (message == null) {
            return null;
        }
        ConversationMessageVO messageVO = new ConversationMessageVO();
        BeanUtils.copyProperties(message, messageVO);
        boolean deleted = Boolean.TRUE.equals(message.getIsDeleted());
        boolean deletedBySender = deleted && Objects.equals(message.getAccountId(), viewerId);
        messageVO.setIsDeleted(deleted);
        messageVO.setDeletedBySender(deletedBySender);
        messageVO.setContent(deleted ? null : message.getContent());
        messageVO.setDisplayContent(deleted
                ? (deletedBySender ? RECALL_BY_SELF : RECALL_BY_PARTNER)
                : message.getContent());
        messageVO.setAvatarUrl(accountMapper.getAvatarUrlById(message.getAccountId()));
        return messageVO;
    }

    public ConversationVO toConversationVO(Conversation conversation, Integer viewerId) {
        if (conversation == null || viewerId == null) {
            return null;
        }
        return toConversationVO(conversation, viewerId, resolvePartnerId(conversation, viewerId));
    }

    public ConversationVO toConversationVO(Conversation conversation, Integer viewerId, Integer partnerId) {
        if (conversation == null) {
            return null;
        }
        ConversationVO conversationVO = ConversationVO.builder()
                .conversationId(conversation.getConversationId())
                .userInfo(getConversationUserInfo(partnerId))
                .updateTime(resolveConversationTime(conversation))
                .pinned(resolvePinned(conversation.getConversationId(), viewerId))
                .partnerOnline(presenceService.isOnline(partnerId))
                .build();
        applyLastMessage(conversationVO, conversation);
        return conversationVO;
    }

    public Integer resolvePartnerId(Conversation conversation, Integer viewerId) {
        if (conversation == null || viewerId == null) {
            return null;
        }
        if (Objects.equals(viewerId, conversation.getAlphaAccountId())) {
            return conversation.getBetaAccountId();
        }
        if (Objects.equals(viewerId, conversation.getBetaAccountId())) {
            return conversation.getAlphaAccountId();
        }
        return null;
    }

    public ConversationMessage getLatestMessage(Integer conversationId) {
        if (conversationId == null) {
            return null;
        }
        return conversationMessageMapper.selectOne(Wrappers.<ConversationMessage>lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .orderByDesc(ConversationMessage::getCreateTime)
                .orderByDesc(ConversationMessage::getConversationMessageId)
                .last("LIMIT 1"));
    }

    private void applyLastMessage(ConversationVO conversationVO, Conversation conversation) {
        ConversationMessage latestMessage = getLatestMessage(conversation.getConversationId());
        if (latestMessage == null) {
            conversationVO.setLastMessageContent("");
            conversationVO.setLastMessageTime(resolveConversationTime(conversation));
            return;
        }
        conversationVO.setLastMessageId(latestMessage.getConversationMessageId());
        conversationVO.setLastMessageSenderId(latestMessage.getAccountId());
        conversationVO.setLastMessageTime(resolveMessageTime(latestMessage));
        conversationVO.setLastMessageContent(Boolean.TRUE.equals(latestMessage.getIsDeleted())
                ? LAST_MESSAGE_RECALLED
                : nullToEmpty(latestMessage.getContent()));
    }

    private UserInfoVO getConversationUserInfo(Integer accountId) {
        UserInfoVO userInfo = accountService.getUserInfo(accountId);
        if (userInfo == null) {
            return null;
        }
        UserInfoVO conversationUserInfo = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, conversationUserInfo);
        conversationUserInfo.setPermission(null);
        return conversationUserInfo;
    }

    private Date resolveConversationTime(Conversation conversation) {
        if (conversation.getUpdateTime() != null) {
            return conversation.getUpdateTime();
        }
        return conversation.getCreateTime();
    }

    private Date resolveMessageTime(ConversationMessage message) {
        if (message.getCreateTime() != null) {
            return message.getCreateTime();
        }
        return message.getUpdateTime();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Boolean resolvePinned(Integer conversationId, Integer viewerId) {
        if (conversationId == null || viewerId == null) {
            return false;
        }
        ConversationUserSetting setting = conversationUserSettingMapper.selectOne(
                Wrappers.<ConversationUserSetting>lambdaQuery()
                        .eq(ConversationUserSetting::getConversationId, conversationId)
                        .eq(ConversationUserSetting::getAccountId, viewerId)
                        .last("LIMIT 1"));
        return setting != null && Boolean.TRUE.equals(setting.getPinned());
    }
}
