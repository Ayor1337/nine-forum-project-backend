package com.ayor.service.impl;

import com.ayor.service.ChatUnreadService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatUnreadServiceImplTest {

    // 测试原子化增加会话并总未读数
    @Test
    void shouldAtomicallyIncreaseConversationAndTotalUnread() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.execute(any(RedisScript.class), any(List.class))).thenReturn("3:8");
        ChatUnreadServiceImpl service = new ChatUnreadServiceImpl(template);

        ChatUnreadService.UnreadCounts result = service.addUnreadAndTotal(7, 2);

        assertEquals(3, result.conversationUnread());
        assertEquals(8, result.totalUnread());
        verify(template).execute(
                any(RedisScript.class),
                org.mockito.ArgumentMatchers.eq(List.of("chat:unread:2:7", "message:user:unread:2"))
        );
    }

    // 测试原子化消费会话未读不带负数总量
    @Test
    void shouldAtomicallyConsumeConversationUnreadWithoutNegativeTotal() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.execute(any(RedisScript.class), any(List.class))).thenReturn("5:0");
        ChatUnreadServiceImpl service = new ChatUnreadServiceImpl(template);

        ChatUnreadService.UnreadCounts result = service.clearUnreadAndTotal(7, 2);

        assertEquals(5, result.conversationUnread());
        assertEquals(0, result.totalUnread());
    }
}
