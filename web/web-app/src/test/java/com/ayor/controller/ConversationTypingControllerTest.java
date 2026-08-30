package com.ayor.controller;

import com.ayor.entity.stomp.ConversationTypingMessage;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationTypingControllerTest {

    // 测试正在输入事件只转发给会话对方
    @Test
    void shouldForwardTypingEventToConversationPartnerOnly() {
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        ConversationMapper conversationMapper = mock(ConversationMapper.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        ConversationTypingController controller = new ConversationTypingController(
                authorizationService,
                conversationMapper,
                messagingTemplate
        );
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                User.withUsername("10").password("n/a").authorities("USER").build(),
                null
        );
        ConversationTypingMessage payload = ConversationTypingMessage.builder()
                .typing(true)
                .build();
        when(conversationMapper.getChatPartnerId(10, 7)).thenReturn(22);

        controller.typing(7, payload, principal);

        verify(authorizationService).assertCanAccessConversation(10, 7);
        verify(messagingTemplate).convertAndSendToUser(
                eq("22"),
                eq("/transfer/conversation/7/typing"),
                any(ConversationTypingMessage.class)
        );
    }
}
