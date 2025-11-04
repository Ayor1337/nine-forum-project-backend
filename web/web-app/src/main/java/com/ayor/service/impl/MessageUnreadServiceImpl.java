package com.ayor.service.impl;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.service.MessageUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageUnreadServiceImpl implements MessageUnreadService {

    private final StringRedisTemplate template;

    private String buildKey(String user) {
        return "message:unread:" + user;
    }

    private boolean existValue(String key) {
        return template.hasKey(key);
    }

    @Override
    public Long getUnread(String user) {
        String key = buildKey(user);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    @Override
    public MessageUnread getUnreadVO(String user) {
        return MessageUnread.builder()
                .unread(getUnread(user))
                .build();
    }

    public void newUnread(String user, Long value) {
        String key = buildKey(user);
        template.opsForValue().set(key, value.toString());
    }

    @Override
    public Long clearUnread(String user, Long value) {
        String key = buildKey(user);

        String unreadCount = template.opsForValue().get(key);
        if (unreadCount == null) {
            return 0L;
        }
        if (value >= Long.parseLong(unreadCount)) {
            template.delete(key);
        }
        if (value < Long.parseLong(unreadCount)) {
           decrUnread(user, value);
        }
        if (Objects.equals(unreadCount, "0")) {
            template.delete(key);
        }
        String remaining = template.opsForValue().get(key);
        return remaining == null ? 0L : Long.parseLong(remaining);
    }

    public long incrUnread(String user, Long value) {
        String key = buildKey(user);
        Long increment = template.opsForValue().increment(key, value);
        return increment == null ? 0 : increment;
    }

    public void decrUnread(String user, Long value) {
        String key = buildKey(user);
        template.opsForValue().decrement(key, value);
    }


    @Override
    public long addUnread(String user, Long value) {
        if (getUnread(user) == 0) {
            newUnread(user, value);
            return 1;
        }
        return incrUnread(user, 1L);
    }
}
