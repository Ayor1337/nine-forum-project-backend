package com.ayor.listener;

import com.ayor.service.PresenceService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StompPresenceEventListenerTest {

    // 测试连接事件使用会话属性记录在线 session
    @Test
    void shouldMarkOnlineFromConnectEventSessionAttributes() {
        PresenceService presenceService = mock(PresenceService.class);
        StompPresenceEventListener listener = new StompPresenceEventListener(presenceService);
        SessionConnectEvent event = new SessionConnectEvent(this, MessageBuilder.withPayload(new byte[0])
                .setHeader("simpSessionId", "s1")
                .setHeader("simpSessionAttributes", Map.of("accountId", "10"))
                .build());

        listener.handleConnect(event);

        verify(presenceService).markOnline(10, "s1");
    }

    // 测试断开事件使用会话属性移除在线 session
    @Test
    void shouldMarkOfflineFromDisconnectEventSessionAttributes() {
        PresenceService presenceService = mock(PresenceService.class);
        StompPresenceEventListener listener = new StompPresenceEventListener(presenceService);
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, MessageBuilder.withPayload(new byte[0])
                .setHeader("simpSessionId", "s1")
                .setHeader("simpSessionAttributes", Map.of("accountId", "10"))
                .build(), "s1", null);

        listener.handleDisconnect(event);

        verify(presenceService).markOffline(10, "s1");
    }
}
