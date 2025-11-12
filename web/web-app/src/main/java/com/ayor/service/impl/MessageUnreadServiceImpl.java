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

    private String buildKey(Integer userId, UnreadMessageType type) {
        return "message:" +
                type.getType() +
                ":unread:" +
                userId;
    }

    private boolean existValue(String key) {
        return template.hasKey(key);
    }

    @Override
    public Long getUnread(Integer userId, UnreadMessageType type) {
        String key = buildKey(userId, type);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    @Override
    public Long getUnread(Integer userId, String type) {
        UnreadMessageType unreadMessageType = Arrays.stream(UnreadMessageType.values())
                .filter(value -> value.getType().equals(type))
                .findAny()
                .orElse(null);
        if (unreadMessageType == null) {
            return 0L;
        }
        String key = buildKey(userId, unreadMessageType);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }

    public Long getAllUnread(Integer userId) {
        Long unreadCount = 0L;
        for (UnreadMessageType value : UnreadMessageType.values()) {
            unreadCount += getUnread(userId, value);
        }
        return unreadCount;
    }

    @Override
    public MessageUnread getUnreadVO(Integer userId, UnreadMessageType type) {
        return MessageUnread.builder()
                .unread(getUnread(userId, type))
                .build();
    }

    @Override
    public MessageUnread getUnreadVO(Integer userId, String type) {
        return MessageUnread.builder()
                .unread(getUnread(userId, type))
                .build();
    }

    @Override
    public MessageUnread getUnreadVO(Integer userId) {
        return MessageUnread.builder()
                .unread(getAllUnread(userId))
                .build();
    }

    public void newUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        template.opsForValue().set(key, value.toString());
    }

    @Override
    public Long clearUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);

        String unreadCount = template.opsForValue().get(key);
        if (unreadCount == null) {
            return 0L;
        }
        if (value >= Long.parseLong(unreadCount)) {
            template.delete(key);
        }
        if (value < Long.parseLong(unreadCount)) {
           decrUnread(userId, type, value);
        }
        if (Objects.equals(unreadCount, "0")) {
            template.delete(key);
        }
        String remaining = template.opsForValue().get(key);
        return remaining == null ? 0L : Long.parseLong(remaining);
    }

    @Override
    public Long clearUnread(Integer userId, UnreadMessageType type) {
        template.delete(buildKey(userId, type));
        return 0L;
    }

    public long incrUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        Long increment = template.opsForValue().increment(key, value);
        return increment == null ? 0 : increment;
    }

    public void decrUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        template.opsForValue().decrement(key, value);
    }


    @Override
    public long addUnread(Integer userId, UnreadMessageType type, Long value) {
        if (getUnread(userId, type) == 0) {
            newUnread(userId, type, value);
            return 1;
        }
        return incrUnread(userId, type, 1L);
    }
}
