package com.ayor.service.impl;

import com.ayor.entity.dto.ConversationMessageDTO;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.util.ConversationViewUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Mock
    private ConversationViewUtils conversationViewUtils;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache conversationCache;

    @Mock
    private Cache conversationListCache;

    // 测试发送消息前先执行发送授权
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

    // 测试发送空消息失败且不写库
    @Test
    void shouldRejectBlankMessageContent() {
        ConversationMessageServiceImpl service = createService();
        ConversationMessageDTO dto = new ConversationMessageDTO();
        dto.setConversationId(7);
        dto.setContent("   ");

        String result = service.sendMessage(dto, 10);

        assertEquals("消息内容不能为空", result);
        verify(authorizationService).assertCanSendConversationMessage(10, 7);
        verify(conversationMessageMapper, never()).insert(any(ConversationMessage.class));
    }

    // 测试发送超长消息失败
    @Test
    void shouldRejectTooLongMessageContent() {
        ConversationMessageServiceImpl service = createService();
        ConversationMessageDTO dto = new ConversationMessageDTO();
        dto.setConversationId(7);
        dto.setContent("a".repeat(1001));

        String result = service.sendMessage(dto, 10);

        assertEquals("消息内容不能超过1000个字符", result);
        verify(conversationMessageMapper, never()).insert(any(ConversationMessage.class));
    }

    // 测试发送成功更新会话时间并推送会话列表项
    @Test
    void shouldUpdateConversationAndPushConversationWhenSendingMessage() {
        ConversationMessageServiceImpl service = createService();
        ConversationMessageDTO dto = new ConversationMessageDTO();
        dto.setConversationId(7);
        dto.setContent(" hello ");
        Account account = new Account();
        account.setAccountId(10);
        Conversation conversation = conversation(7, 10, 22);
        ConversationMessageVO messageVO = new ConversationMessageVO();

        when(accountMapper.getAccountById(10)).thenReturn(account);
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(conversationMessageMapper.insert(any(ConversationMessage.class))).thenReturn(1);
        when(conversationViewUtils.toMessageVO(any(ConversationMessage.class), eq(10))).thenReturn(messageVO);
        when(conversationViewUtils.toMessageVO(any(ConversationMessage.class), eq(22))).thenReturn(messageVO);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.sendMessage(dto, 10);

        assertNull(result);
        ArgumentCaptor<ConversationMessage> messageCaptor = ArgumentCaptor.forClass(ConversationMessage.class);
        verify(conversationMessageMapper).insert(messageCaptor.capture());
        assertEquals("hello", messageCaptor.getValue().getContent());
        verify(conversationMapper).updateById(conversation);
        verify(simpMessagingTemplate).convertAndSendToUser("10", "/notif/conversations", null);
        verify(simpMessagingTemplate).convertAndSendToUser("22", "/notif/conversations", null);
        verify(conversationCache).evict("10:22");
        verify(conversationCache).evict("10:10:22");
        verify(conversationCache).evict("22:10:22");
    }

    // 测试只能撤回本人消息
    @Test
    void shouldRejectRecallingOtherUserMessage() {
        ConversationMessageServiceImpl service = createService();
        Conversation conversation = conversation(7, 10, 22);
        ConversationMessage message = message(9, 7, 22, new Date());
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(conversationMessageMapper.selectById(9)).thenReturn(message);

        String result = service.recallMessage(7, 9, 10);

        assertEquals("只能撤回自己发送的消息", result);
        verify(conversationMessageMapper, never()).updateById(any(ConversationMessage.class));
    }

    // 测试不能撤回非本会话消息
    @Test
    void shouldRejectRecallingMessageOutsideConversation() {
        ConversationMessageServiceImpl service = createService();
        Conversation conversation = conversation(7, 10, 22);
        ConversationMessage message = message(9, 8, 10, new Date());
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(conversationMessageMapper.selectById(9)).thenReturn(message);

        String result = service.recallMessage(7, 9, 10);

        assertEquals("消息不存在", result);
        verify(conversationMessageMapper, never()).updateById(any(ConversationMessage.class));
    }

    // 测试超过撤回时间窗口失败
    @Test
    void shouldRejectRecallingExpiredMessage() {
        ConversationMessageServiceImpl service = createService();
        Conversation conversation = conversation(7, 10, 22);
        ConversationMessage message = message(9, 7, 10, new Date(System.currentTimeMillis() - 121_000L));
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(conversationMessageMapper.selectById(9)).thenReturn(message);

        String result = service.recallMessage(7, 9, 10);

        assertEquals("消息已超过可撤回时间", result);
        verify(conversationMessageMapper, never()).updateById(any(ConversationMessage.class));
    }

    // 测试撤回成功隐藏内容并推送同一消息 ID
    @Test
    void shouldRecallMessageAndPushUpdatedMessage() {
        ConversationMessageServiceImpl service = createService();
        Conversation conversation = conversation(7, 10, 22);
        ConversationMessage message = message(9, 7, 10, new Date());
        ConversationMessageVO senderVO = new ConversationMessageVO();
        senderVO.setConversationMessageId(9);
        senderVO.setDisplayContent("你撤回了一条消息");
        ConversationMessageVO partnerVO = new ConversationMessageVO();
        partnerVO.setConversationMessageId(9);
        partnerVO.setDisplayContent("对方撤回了一条消息");

        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(conversationMessageMapper.selectById(9)).thenReturn(message);
        when(conversationMessageMapper.updateById(message)).thenReturn(1);
        when(conversationViewUtils.toMessageVO(message, 10)).thenReturn(senderVO);
        when(conversationViewUtils.toMessageVO(message, 22)).thenReturn(partnerVO);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.recallMessage(7, 9, 10);

        assertNull(result);
        assertNull(message.getContent());
        assertEquals(true, message.getIsDeleted());
        verify(simpMessagingTemplate).convertAndSendToUser("10", "/transfer/conversation/7", senderVO);
        verify(simpMessagingTemplate).convertAndSendToUser("22", "/transfer/conversation/7", partnerVO);
    }

    private ConversationMessageServiceImpl createService() {
        ConversationMessageServiceImpl service = new ConversationMessageServiceImpl(
                accountMapper,
                conversationMapper,
                simpMessagingTemplate,
                authorizationService,
                conversationViewUtils,
                cacheManager
        );
        ReflectionTestUtils.setField(service, "baseMapper", conversationMessageMapper);
        return service;
    }

    private Conversation conversation(Integer conversationId, Integer alphaId, Integer betaId) {
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        conversation.setAlphaAccountId(alphaId);
        conversation.setBetaAccountId(betaId);
        conversation.setIsDeleted(false);
        return conversation;
    }

    private ConversationMessage message(Integer messageId, Integer conversationId, Integer accountId, Date createTime) {
        ConversationMessage message = new ConversationMessage();
        message.setConversationMessageId(messageId);
        message.setConversationId(conversationId);
        message.setAccountId(accountId);
        message.setContent("hello");
        message.setCreateTime(createTime);
        message.setIsDeleted(false);
        return message;
    }
}
