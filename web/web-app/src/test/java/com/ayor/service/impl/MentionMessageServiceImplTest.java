package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.MentionMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.MentionMessageMapper;
import com.ayor.service.MessageUnreadService;
import com.ayor.service.UserRelationService;
import com.ayor.type.AccountStatus;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentionMessageServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private MessageUnreadService messageUnreadService;

    @Mock
    private MentionMessageMapper mentionMessageMapper;

    private MentionMessageServiceImpl mentionMessageService;

    @BeforeEach
    void setUp() {
        mentionMessageService = new MentionMessageServiceImpl(
                new TipTapUtils(),
                accountMapper,
                userRelationService,
                messagingTemplate,
                stompUtils,
                messageUnreadService
        );
        ReflectionTestUtils.setField(mentionMessageService, "baseMapper", mentionMessageMapper);
    }

    @Test
    void shouldCreateMentionMessageForValidThreadMention() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "mention",
                          "attrs": {
                            "accountId": 2,
                            "username": "alice"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        Account fromAccount = createAccount(1, "author", AccountStatus.ACTIVE, false);
        Account targetAccount = createAccount(2, "alice", AccountStatus.ACTIVE, false);
        when(accountMapper.getAccountById(1)).thenReturn(fromAccount);
        when(accountMapper.getAccountsByIds(List.of(2))).thenReturn(List.of(targetAccount));
        when(userRelationService.isBlockedEitherDirection(1, 2)).thenReturn(false);
        when(stompUtils.isUserSubscribed("2", "/notif/mention")).thenReturn(false);
        when(stompUtils.isUserSubscribed("2", "/notif/unread")).thenReturn(false);
        when(stompUtils.isUserSubscribed("2", "/notif/unread/mention")).thenReturn(false);
        when(mentionMessageMapper.insert(any(MentionMessage.class))).thenReturn(1);

        mentionMessageService.createThreadMentionMessages(content, 1, 99);

        ArgumentCaptor<MentionMessage> captor = ArgumentCaptor.forClass(MentionMessage.class);
        verify(mentionMessageMapper).insert(captor.capture());
        MentionMessage saved = captor.getValue();
        assertEquals(2, saved.getAccountId());
        assertEquals(1, saved.getFromAccountId());
        assertEquals("THREAD", saved.getSourceType());
        assertEquals(99, saved.getSourceId());
        assertEquals("/threads/99", saved.getPath());
        verify(messageUnreadService).addUnread(2, UnreadMessageType.MENTION_MESSAGE, 1L);
    }

    @Test
    void shouldSkipMentionWhenTargetIsSelfOrBlocked() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "mention",
                          "attrs": {
                            "accountId": 1,
                            "username": "author"
                          }
                        },
                        {
                          "type": "mention",
                          "attrs": {
                            "accountId": 2,
                            "username": "alice"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        Account fromAccount = createAccount(1, "author", AccountStatus.ACTIVE, false);
        Account blockedTarget = createAccount(2, "alice", AccountStatus.ACTIVE, false);
        when(accountMapper.getAccountById(1)).thenReturn(fromAccount);
        when(accountMapper.getAccountsByIds(List.of(1, 2))).thenReturn(List.of(fromAccount, blockedTarget));
        when(userRelationService.isBlockedEitherDirection(1, 2)).thenReturn(true);

        mentionMessageService.createThreadMentionMessages(content, 1, 99);

        verify(mentionMessageMapper, never()).insert(any(MentionMessage.class));
        verify(messageUnreadService, never()).addUnread(eq(2), eq(UnreadMessageType.MENTION_MESSAGE), eq(1L));
    }

    private Account createAccount(Integer accountId, String username, AccountStatus status, boolean deleted) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(username);
        account.setNickname(username);
        account.setAvatarUrl("avatar");
        account.setStatus(status.getCode());
        account.setDeleted(deleted);
        return account;
    }
}
