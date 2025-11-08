package com.ayor.service.impl;

import com.ayor.service.ChatUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatUnreadServiceImpl implements ChatUnreadService {

    private final StringRedisTemplate template;

    private String buildKey(Integer conversationId, String fromUser) {
        return "chat:unread:" + fromUser + ":" + conversationId;
    }

    private boolean existValue(String key) {
        return template.hasKey(key);
    }

    @Override
    public Long getUnread(Integer conversationId, String fromUser) {
        String key = buildKey(conversationId, fromUser);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    public void newUnread(Integer conversationId, String fromUser) {
        String key = buildKey(conversationId, fromUser);
        template.opsForValue().set(key, "1");
    }

    @Override
    public long clearUnread(Integer conversationId, String fromUser) {
        String key = buildKey(conversationId, fromUser);
        if (!existValue(key)) {
            return 0;
        }
        String consume = template.opsForValue().get(key);
        template.delete(key);
        if (consume != null) {
            return Long.parseLong(consume);
        }
        return 0L;
    }

    public long incrUnread(Integer conversationId, String fromUser) {
        String key = buildKey(conversationId, fromUser);
        Long increment = template.opsForValue().increment(key);
        return increment == null ? 0 : increment;
    }

    public void decrUnread(Integer conversationId, String fromUser) {
        String key = buildKey(conversationId, fromUser);
        template.opsForValue().decrement(key);
    }


    @Override
    public long addUnread(Integer conversationId, String fromUser) {
        if (getUnread(conversationId, fromUser) == 0) {
            newUnread(conversationId, fromUser);
            return 1;
        }
        return incrUnread(conversationId, fromUser);
    }
}
