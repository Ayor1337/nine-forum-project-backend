package com.ayor.service.impl;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.service.MessageUnreadService;
import com.ayor.type.UnreadMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageUnreadServiceImpl implements MessageUnreadService {

    private final StringRedisTemplate template;

    private String buildKey(String user, UnreadMessageType type) {
        return "message:" +
                type.getType() +
                ":unread:" +
                user;
    }

    private boolean existValue(String key) {
        return template.hasKey(key);
    }

    @Override
    public Long getUnread(String user, UnreadMessageType type) {
        String key = buildKey(user, type);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    @Override
    public Long getUnread(String user, String type) {
        UnreadMessageType unreadMessageType = Arrays.stream(UnreadMessageType.values())
                .findAny()
                .filter(value -> value.getType().equals(type))
                .orElse(null);
        if (unreadMessageType == null) {
            return 0L;
        }
        String key = buildKey(user, unreadMessageType);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    public Long getAllUnread(String user) {
        Long unreadCount = 0L;
        for (UnreadMessageType value : UnreadMessageType.values()) {
            unreadCount += getUnread(user, value);
        }
        return unreadCount;
    }

    @Override
    public MessageUnread getUnreadVO(String user, UnreadMessageType type) {
        return MessageUnread.builder()
                .unread(getUnread(user, type))
                .build();
    }

    @Override
    public MessageUnread getUnreadVO(String user, String type) {
        return MessageUnread.builder()
                .unread(getUnread(user, type))
                .build();
    }

    @Override
    public MessageUnread getUnreadVO(String user) {
        return MessageUnread.builder()
                .unread(getAllUnread(user))
                .build();
    }

    public void newUnread(String user, UnreadMessageType type, Long value) {
        String key = buildKey(user, type);
        template.opsForValue().set(key, value.toString());
    }

    @Override
    public Long clearUnread(String user, UnreadMessageType type, Long value) {
        String key = buildKey(user, type);

        String unreadCount = template.opsForValue().get(key);
        if (unreadCount == null) {
            return 0L;
        }
        if (value >= Long.parseLong(unreadCount)) {
            template.delete(key);
        }
        if (value < Long.parseLong(unreadCount)) {
           decrUnread(user, type, value);
        }
        if (Objects.equals(unreadCount, "0")) {
            template.delete(key);
        }
        String remaining = template.opsForValue().get(key);
        return remaining == null ? 0L : Long.parseLong(remaining);
    }

    @Override
    public Long clearUnread(String user, UnreadMessageType type) {
        template.delete(buildKey(user, type));
        return 0L;
    }

    public long incrUnread(String user, UnreadMessageType type, Long value) {
        String key = buildKey(user, type);
        Long increment = template.opsForValue().increment(key, value);
        return increment == null ? 0 : increment;
    }

    public void decrUnread(String user, UnreadMessageType type, Long value) {
        String key = buildKey(user, type);
        template.opsForValue().decrement(key, value);
    }


    @Override
    public long addUnread(String user, UnreadMessageType type, Long value) {
        if (getUnread(user, type) == 0) {
            newUnread(user, type, value);
            return 1;
        }
        return incrUnread(user, type, 1L);
    }
}
