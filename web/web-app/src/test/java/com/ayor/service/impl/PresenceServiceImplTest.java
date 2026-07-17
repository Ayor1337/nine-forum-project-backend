package com.ayor.service.impl;

import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.PresenceMessage;
import com.ayor.mapper.ConversationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    // 测试用户从离线变为在线时只推送给会话对方
    @Test
    void shouldPushOnlinePresenceToConversationPartners() {
        PresenceServiceImpl service = createService();
        Conversation conversation = conversation(7, 10, 22);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("presence:user:10")).thenReturn(0L);
        when(conversationMapper.selectList(any())).thenReturn(List.of(conversation));

        service.markOnline(10, "session-a");

        verify(setOperations).add("presence:user:10", "session-a");
        ArgumentCaptor<PresenceMessage> captor = ArgumentCaptor.forClass(PresenceMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("22"), eq("/notif/presence"), captor.capture());
        assertEquals(10, captor.getValue().getUserId());
        assertEquals(true, captor.getValue().getOnline());
    }

    // 测试多连接时断开一个 session 不推下线
    @Test
    void shouldNotPushOfflineWhenOtherSessionsRemain() {
        PresenceServiceImpl service = createService();
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("presence:user:10")).thenReturn(1L);

        service.markOffline(10, "session-a");

        verify(setOperations).remove("presence:user:10", "session-a");
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    private PresenceServiceImpl createService() {
        return new PresenceServiceImpl(redisTemplate, conversationMapper, messagingTemplate);
    }

    private Conversation conversation(Integer conversationId, Integer alphaId, Integer betaId) {
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        conversation.setAlphaAccountId(alphaId);
        conversation.setBetaAccountId(betaId);
        conversation.setIsDeleted(false);
        return conversation;
    }
}
