package com.ayor.service.impl;

import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.PresenceMessage;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.PresenceService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 在线状态服务实现
 */
@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "presence:user:";

    private static final String NOTIF_PRESENCE = "/notif/presence";

    private final StringRedisTemplate redisTemplate;

    private final ConversationMapper conversationMapper;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void markOnline(Integer accountId, String sessionId) {
        if (accountId == null || sessionId == null) {
            return;
        }
        String key = presenceKey(accountId);
        boolean wasOffline = !isOnline(accountId);
        redisTemplate.opsForSet().add(key, sessionId);
        if (wasOffline) {
            pushPresenceToConversationPartners(accountId, true);
        }
    }

    @Override
    public void markOffline(Integer accountId, String sessionId) {
        if (accountId == null || sessionId == null) {
            return;
        }
        String key = presenceKey(accountId);
        redisTemplate.opsForSet().remove(key, sessionId);
        if (!isOnline(accountId)) {
            pushPresenceToConversationPartners(accountId, false);
        }
    }

    @Override
    public boolean isOnline(Integer accountId) {
        if (accountId == null) {
            return false;
        }
        Long size = redisTemplate.opsForSet().size(presenceKey(accountId));
        return size != null && size > 0;
    }

    private void pushPresenceToConversationPartners(Integer accountId, boolean online) {
        PresenceMessage message = PresenceMessage.builder()
                .userId(accountId)
                .online(online)
                .time(new Date())
                .build();
        for (Integer partnerId : getConversationPartnerIds(accountId)) {
            messagingTemplate.convertAndSendToUser(partnerId.toString(), NOTIF_PRESENCE, message);
        }
    }

    private Set<Integer> getConversationPartnerIds(Integer accountId) {
        List<Conversation> conversations = conversationMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .and(wrapper -> wrapper.eq(Conversation::getAlphaAccountId, accountId)
                        .or()
                        .eq(Conversation::getBetaAccountId, accountId))
                .and(wrapper -> wrapper.eq(Conversation::getIsDeleted, false)
                        .or()
                        .isNull(Conversation::getIsDeleted)));
        Set<Integer> partnerIds = new HashSet<>();
        for (Conversation conversation : conversations) {
            Integer partnerId = Objects.equals(accountId, conversation.getAlphaAccountId())
                    ? conversation.getBetaAccountId()
                    : conversation.getAlphaAccountId();
            if (partnerId != null) {
                partnerIds.add(partnerId);
            }
        }
        return partnerIds;
    }

    private String presenceKey(Integer accountId) {
        return PRESENCE_KEY_PREFIX + accountId;
    }
}
