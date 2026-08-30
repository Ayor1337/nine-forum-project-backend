package com.ayor.service.impl;

import com.ayor.type.UnreadMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageUnreadServiceImplTest {

    // 测试新增未读时使用指定增量
    @Test
    void addUnreadShouldUseRequestedIncrement() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(values.increment("message:reply:unread:7", 4L)).thenReturn(9L);
        MessageUnreadServiceImpl service = new MessageUnreadServiceImpl(template);

        assertEquals(9, service.addUnread(7, UnreadMessageType.REPLY_MESSAGE, 4L));
        verify(values).increment("message:reply:unread:7", 4L);
    }

    // 测试清理未读时使用归零保护Lua脚本
    @Test
    void clearUnreadShouldUseAtomicFloorAtZeroScript() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.execute(any(RedisScript.class), eq(List.of("message:reply:unread:7")), eq("20")))
                .thenReturn(0L);
        MessageUnreadServiceImpl service = new MessageUnreadServiceImpl(template);

        assertEquals(0, service.clearUnread(7, UnreadMessageType.REPLY_MESSAGE, 20L));
    }

    // 测试未读概览包含关注动态未读
    @Test
    void unreadOverviewShouldIncludeFollowUnread() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(values.get("message:reply:unread:7")).thenReturn("1");
        when(values.get("message:mention:unread:7")).thenReturn("2");
        when(values.get("message:follow:unread:7")).thenReturn("3");
        when(values.get("message:system:unread:7")).thenReturn("4");
        when(values.get("message:user:unread:7")).thenReturn("5");
        MessageUnreadServiceImpl service = new MessageUnreadServiceImpl(template);

        assertEquals(15, service.getUnreadOverviewVO(7).getTotal());
        assertEquals(3, service.getUnreadOverviewVO(7).getFollow());
        assertEquals(3, service.getUnreadVO(7, "follow").getUnread());
    }
}
