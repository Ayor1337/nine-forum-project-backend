package com.ayor.service.impl;

import com.ayor.aspect.chat.ChatNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ConversationMessageDTO;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.ayor.service.AuthorizationService;
import com.ayor.util.ConversationViewUtils;
import com.ayor.type.NotificationType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 会话消息服务实现
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements ConversationMessageService {

    private static final int MAX_TEXT_LENGTH = 1000;

    private static final long RECALL_WINDOW_MILLIS = 2 * 60 * 1000L;

    private static final String CONVERSATION_CACHE = "conversation";

    private static final String CONVERSATION_LIST_CACHE = "conversationList";

    private final AccountMapper accountMapper;

    private final ConversationMapper conversationMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final AuthorizationService authorizationService;

    private final ConversationViewUtils conversationViewUtils;

    private final CacheManager cacheManager;

    /**
     * 发送会话消息并触发通知。
     */
    @Override
    @ChatNotif(conversationId = "#conversationMessage.conversationId",
            type = NotificationType.SEND_MSG,
            userId = "#accountId")
    public String sendMessage(ConversationMessageDTO conversationMessage, Integer accountId) {
        authorizationService.assertCanSendConversationMessage(accountId, conversationMessage.getConversationId());
        String content = normalizeContent(conversationMessage.getContent());
        if (!StringUtils.hasText(content)) {
            return "消息内容不能为空";
        }
        if (content.length() > MAX_TEXT_LENGTH) {
            return "消息内容不能超过1000个字符";
        }
        Account account = accountMapper.getAccountById(accountId);
        if(account == null) {
            return "用户不存在";
        }
        Conversation conversation = conversationMapper.selectById(conversationMessage.getConversationId());
        if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
            return "会话不存在";
        }
        Date now = new Date();
        ConversationMessage message = ConversationMessage.builder()
                .conversationId(conversationMessage.getConversationId())
                .accountId(account.getAccountId())
                .content(content)
                .createTime(now)
                .updateTime(now)
                .isDeleted(false)
                .isEdit(false)
                .build();

        if (this.save(message)) {
            touchConversation(conversation, now);
            pushMessageToParticipants(conversation, message);
            pushConversationToParticipants(conversation);
            evictConversationCaches(conversation);
            return null;
        }
        return "发送失败";

    }

    /**
     * 撤回指定私信消息并向双方推送同 ID 更新。
     */
    @Override
    public String recallMessage(Integer conversationId, Integer messageId, Integer accountId) {
        authorizationService.assertCanAccessConversation(accountId, conversationId);
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
            return "会话不存在";
        }
        ConversationMessage message = this.getById(messageId);
        if (message == null || !Objects.equals(message.getConversationId(), conversationId)) {
            return "消息不存在";
        }
        if (!Objects.equals(message.getAccountId(), accountId)) {
            return "只能撤回自己发送的消息";
        }
        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            return null;
        }
        Date now = new Date();
        if (message.getCreateTime() == null || now.getTime() - message.getCreateTime().getTime() > RECALL_WINDOW_MILLIS) {
            return "消息已超过可撤回时间";
        }
        message.setIsDeleted(true);
        message.setContent(null);
        message.setUpdateTime(now);
        if (!this.updateById(message)) {
            return "撤回失败";
        }
        touchConversation(conversation, now);
        pushMessageToParticipants(conversation, message);
        pushConversationToParticipants(conversation);
        evictConversationCaches(conversation);
        return null;
    }

    /**
     * 分页获取会话消息列表。
     */
    @Override
    @ChatNotif(conversationId = "#conversationId",
            type = NotificationType.RECEIVED_MSG, userId = "#accountId")
    public PageEntity<ConversationMessageVO> getConversationMessageList(Integer conversationId, Integer accountId, Integer pageNum) {
        authorizationService.assertCanAccessConversation(accountId, conversationId);
        Page<ConversationMessage> page = this.lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .orderByDesc(ConversationMessage::getCreateTime)
                .page(Page.of(pageNum, 20));
        List<ConversationMessageVO> conversationMessageVOS = new ArrayList<>();
        page.getRecords().forEach(message -> {
            conversationMessageVOS.add(conversationViewUtils.toMessageVO(message, accountId));
        });

        return new PageEntity<>(page.getTotal(), conversationMessageVOS);
    }

    private String normalizeContent(String content) {
        return content == null ? null : content.trim();
    }

    private void touchConversation(Conversation conversation, Date updateTime) {
        conversation.setHidden(0);
        conversation.setUpdateTime(updateTime);
        conversationMapper.updateById(conversation);
    }

    private void pushMessageToParticipants(Conversation conversation, ConversationMessage message) {
        pushMessage(conversation.getAlphaAccountId(), conversation, message);
        pushMessage(conversation.getBetaAccountId(), conversation, message);
    }

    private void pushMessage(Integer viewerId, Conversation conversation, ConversationMessage message) {
        if (viewerId == null) {
            return;
        }
        ConversationMessageVO messageVO = conversationViewUtils.toMessageVO(message, viewerId);
        simpMessagingTemplate.convertAndSendToUser(
                viewerId.toString(),
                "/transfer/conversation/" + conversation.getConversationId(),
                messageVO
        );
    }

    private void pushConversationToParticipants(Conversation conversation) {
        pushConversation(conversation.getAlphaAccountId(), conversation);
        pushConversation(conversation.getBetaAccountId(), conversation);
    }

    private void pushConversation(Integer viewerId, Conversation conversation) {
        if (viewerId == null) {
            return;
        }
        ConversationVO conversationVO = conversationViewUtils.toConversationVO(conversation, viewerId);
        simpMessagingTemplate.convertAndSendToUser(viewerId.toString(), "/notif/conversations", conversationVO);
    }

    private void evictConversationCaches(Conversation conversation) {
        evictConversationCache(conversation);
        evictConversationListCache(conversation.getAlphaAccountId());
        evictConversationListCache(conversation.getBetaAccountId());
    }

    private void evictConversationCache(Conversation conversation) {
        Cache cache = cacheManager.getCache(CONVERSATION_CACHE);
        if (cache != null) {
            String pairKey = ConversationServiceImpl.conversationPairKey(
                    conversation.getAlphaAccountId(),
                    conversation.getBetaAccountId());
            cache.evict(pairKey);
            cache.evict(ConversationServiceImpl.conversationViewerCacheKey(
                    conversation.getAlphaAccountId(),
                    conversation.getAlphaAccountId(),
                    conversation.getBetaAccountId()));
            cache.evict(ConversationServiceImpl.conversationViewerCacheKey(
                    conversation.getBetaAccountId(),
                    conversation.getAlphaAccountId(),
                    conversation.getBetaAccountId()));
        }
    }

    private void evictConversationListCache(Integer accountId) {
        Cache cache = cacheManager.getCache(CONVERSATION_LIST_CACHE);
        if (cache != null && accountId != null) {
            cache.evict(accountId);
        }
    }

}
