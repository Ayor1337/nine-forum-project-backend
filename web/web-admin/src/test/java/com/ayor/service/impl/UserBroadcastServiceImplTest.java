package com.ayor.service.impl;

import com.ayor.entity.dto.UserBroadcastDTO;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBroadcastServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldSendSystemMessageToEveryValidAccount() {
        UserBroadcastServiceImpl service = new UserBroadcastServiceImpl(accountMapper, rabbitTemplate);
        UserBroadcastDTO dto = new UserBroadcastDTO();
        dto.setAccountIds(List.of(1, 2));
        dto.setTitle("系统通知");
        dto.setContent("维护公告");

        when(accountMapper.selectById(1)).thenReturn(account(1, false));
        when(accountMapper.selectById(2)).thenReturn(account(2, false));

        String result = service.sendUserBroadcast(dto);

        assertNull(result);
        ArgumentCaptor<UserSystemMessage<String>> captor = ArgumentCaptor.forClass(UserSystemMessage.class);
        verify(rabbitTemplate, times(2)).convertAndSend(eq("broadcast.direct"), eq("broadcast"), captor.capture());
        assertEquals(List.of(1, 2), captor.getAllValues().stream().map(UserSystemMessage::getSendTo).toList());
    }

    @Test
    void shouldRejectWholeBroadcastWhenAnyAccountIsDeleted() {
        UserBroadcastServiceImpl service = new UserBroadcastServiceImpl(accountMapper, rabbitTemplate);
        UserBroadcastDTO dto = new UserBroadcastDTO();
        dto.setAccountIds(List.of(1, 2));
        dto.setTitle("系统通知");
        dto.setContent("维护公告");

        when(accountMapper.selectById(1)).thenReturn(account(1, false));
        when(accountMapper.selectById(2)).thenReturn(account(2, true));

        String result = service.sendUserBroadcast(dto);

        assertEquals("存在无效用户：2", result);
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    private Account account(Integer accountId, boolean deleted) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setDeleted(deleted);
        return account;
    }
}
