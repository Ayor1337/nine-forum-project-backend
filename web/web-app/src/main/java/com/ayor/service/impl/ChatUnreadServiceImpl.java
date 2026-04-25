package com.ayor.service.impl;

import com.ayor.service.ChatUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatUnreadServiceImpl implements ChatUnreadService {

    private final StringRedisTemplate template;
    /**
     * buildKey 方法。
     */


    private String buildKey(Integer conversationId, Integer fromUserId) {
        return "chat:unread:" + fromUserId + ":" + conversationId;
    }
    /**
     * existValue 方法。
     */

    private boolean existValue(String key) {
        return template.hasKey(key);
    }
    /**
     * getUnread 方法。
     */

    @Override
    public Long getUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }
    /**
     * newUnread 方法。
     */

    public void newUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        template.opsForValue().set(key, "1");
    }
    /**
     * clearUnread 方法。
     */

    @Override
    public long clearUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
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
    /**
     * incrUnread 方法。
     */

    public long incrUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        Long increment = template.opsForValue().increment(key);
        return increment == null ? 0 : increment;
    }
    /**
     * decrUnread 方法。
     */

    public void decrUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        template.opsForValue().decrement(key);
    }
    /**
     * addUnread 方法。
     */


    @Override
    public long addUnread(Integer conversationId, Integer fromUserId) {
        if (getUnread(conversationId, fromUserId) == 0) {
            newUnread(conversationId, fromUserId);
            return 1;
        }
        return incrUnread(conversationId, fromUserId);
    }
}
