package com.ayor.service.impl;

import com.ayor.entity.dto.ConversationMessageDTO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ConversationMessageServiceImplTest {

    @Mock
    private ConversationMessageMapper conversationMessageMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private AuthorizationService authorizationService;

    @Test
    void shouldUseSendAuthorizationBeforeSendingMessage() {
        ConversationMessageServiceImpl service = createService();
        ConversationMessageDTO dto = new ConversationMessageDTO();
        dto.setConversationId(7);
        dto.setContent("hello");
        doThrow(new AccessDeniedException("Access denied"))
                .when(authorizationService).assertCanSendConversationMessage(10, 7);

        assertThrows(AccessDeniedException.class, () -> service.sendMessage(dto, 10));

        verify(authorizationService).assertCanSendConversationMessage(10, 7);
        verifyNoInteractions(accountMapper, conversationMapper, simpMessagingTemplate);
    }

    private ConversationMessageServiceImpl createService() {
        ConversationMessageServiceImpl service = new ConversationMessageServiceImpl(
                accountMapper,
                conversationMapper,
                simpMessagingTemplate,
                authorizationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", conversationMessageMapper);
        return service;
    }
}
