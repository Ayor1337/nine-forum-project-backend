package com.ayor.service.impl;

import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.entity.pojo.SystemMessage;
import com.ayor.entity.vo.SystemMessageVO;
import com.ayor.mapper.SystemMessageMapper;
import com.ayor.type.UserViolationType;
import com.ayor.util.STOMPUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private SystemMessageMapper systemMessageMapper;

    // 测试用户系统广播持久化并推送当用户已订阅
    @Test
    void userSystemBroadcastPersistsAndPushesWhenUserSubscribed() {
        BroadcastServiceImpl service = new BroadcastServiceImpl(messagingTemplate, stompUtils, systemMessageMapper);
        when(stompUtils.isUserSubscribed("7", "/notif/system")).thenReturn(true);

        service.userSystemBroadcast(new UserSystemMessage<>("content", "title", 7));

        ArgumentCaptor<SystemMessage> entityCaptor = ArgumentCaptor.forClass(SystemMessage.class);
        ArgumentCaptor<SystemMessageVO> voCaptor = ArgumentCaptor.forClass(SystemMessageVO.class);
        verify(systemMessageMapper).insert(entityCaptor.capture());
        verify(messagingTemplate).convertAndSendToUser(eq("7"), eq("/notif/system"), voCaptor.capture());
        assertThat(entityCaptor.getValue().getTitle()).isEqualTo("title");
        assertThat(entityCaptor.getValue().getContent()).isEqualTo("content");
        assertThat(entityCaptor.getValue().getAccountId()).isEqualTo(7);
        assertThat(voCaptor.getValue().getTitle()).isEqualTo("title");
        assertThat(voCaptor.getValue().getContent()).isEqualTo("content");
    }

    // 测试用户未订阅时违规广播不推送
    @Test
    void userViolationBroadcastDoesNotPushWhenUserNotSubscribed() {
        BroadcastServiceImpl service = new BroadcastServiceImpl(messagingTemplate, stompUtils, systemMessageMapper);
        when(stompUtils.isUserSubscribed("7", "/notif/system")).thenReturn(false);

        service.userViolationBroadcast(
                new UserViolationMessage<>("violation", "notice", 7, UserViolationType.AVATAR_VIOLATION));

        verify(systemMessageMapper).insert(any(SystemMessage.class));
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }
}
