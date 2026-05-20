package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.MentionMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.MentionMessageMapper;
import com.ayor.service.MessageUnreadService;
import com.ayor.service.UserRelationService;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentionMessageServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private MentionMessageMapper mentionMessageMapper;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private MessageUnreadService messageUnreadService;

    @Test
    void shouldCreateMentionMessageWhenMentionLabelDiffersFromUsername() {
        MentionMessageServiceImpl service = new MentionMessageServiceImpl(
                new TipTapUtils(),
                accountMapper,
                userRelationService,
                messagingTemplate,
                stompUtils,
                messageUnreadService
        );
        ReflectionTestUtils.setField(service, "baseMapper", mentionMessageMapper);

        Account fromAccount = new Account();
        fromAccount.setAccountId(1);
        fromAccount.setUsername("author");
        fromAccount.setStatus(1);

        Account targetAccount = new Account();
        targetAccount.setAccountId(2);
        targetAccount.setUsername("alice");
        targetAccount.setStatus(1);

        when(accountMapper.getAccountById(1)).thenReturn(fromAccount);
        when(accountMapper.getAccountsByIds(List.of(2))).thenReturn(List.of(targetAccount));
        when(userRelationService.isBlockedEitherDirection(1, 2)).thenReturn(false);
        when(mentionMessageMapper.insert(any(MentionMessage.class))).thenReturn(1);

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
                            "label": "Alice Nick"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        service.createThreadMentionMessages(content, 1, 10);

        verify(mentionMessageMapper, times(1)).insert(any(MentionMessage.class));
    }
}
