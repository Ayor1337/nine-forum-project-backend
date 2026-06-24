package com.ayor.service.impl;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
import com.ayor.service.MessageUnreadService;
import com.ayor.type.UnreadMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageUnreadServiceImpl implements MessageUnreadService {

    private static final DefaultRedisScript<Long> DECREMENT_SCRIPT = new DefaultRedisScript<>(
            "local current = tonumber(redis.call('GET', KEYS[1]) or '0'); " +
                    "local remaining = current - tonumber(ARGV[1]); " +
                    "if remaining <= 0 then redis.call('DEL', KEYS[1]); return 0; end; " +
                    "redis.call('SET', KEYS[1], remaining); return remaining;",
            Long.class
    );

    private final StringRedisTemplate template;
    /**
     * 构造 Redis 中使用的 key。
     */

    private String buildKey(Integer userId, UnreadMessageType type) {
        return "message:" +
                type.getType() +
                ":unread:" +
                userId;
    }
    /**
     * 判断指定 Redis key 是否存在。
     */

    @Override
    public Long getUnread(Integer userId, UnreadMessageType type) {
        String key = buildKey(userId, type);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }
    /**
     * 获取指定用户的未读数量。
     */

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
    /**
     * 汇总当前用户的全部未读消息数量。
     */

    public Long getAllUnread(Integer userId) {
        Long unreadCount = 0L;
        for (UnreadMessageType value : UnreadMessageType.values()) {
            unreadCount += getUnread(userId, value);
        }
        return unreadCount;
    }
    /**
     * 构造指定类型未读消息的展示对象。
     */

    @Override
    public MessageUnread getUnreadVO(Integer userId, UnreadMessageType type) {
        return MessageUnread.builder()
                .unread(getUnread(userId, type))
                .build();
    }
    /**
     * 构造指定类型未读消息的展示对象。
     */

    @Override
    public MessageUnread getUnreadVO(Integer userId, String type) {
        return MessageUnread.builder()
                .unread(getUnread(userId, type))
                .build();
    }
    /**
     * 构造当前用户全部未读消息的展示对象。
     */

    @Override
    public MessageUnread getUnreadVO(Integer userId) {
        return MessageUnread.builder()
                .unread(getAllUnread(userId))
                .build();
    }
    /**
     * 构造当前用户全部未读消息的概览对象。
     */

    @Override
    public UnreadOverviewVO getUnreadOverviewVO(Integer userId) {
        Long reply = getUnread(userId, UnreadMessageType.REPLY_MESSAGE);
        Long mention = getUnread(userId, UnreadMessageType.MENTION_MESSAGE);
        Long system = getUnread(userId, UnreadMessageType.SYSTEM_MESSAGE);
        Long user = getUnread(userId, UnreadMessageType.USER_MESSAGE);

        return UnreadOverviewVO.builder()
                .total(reply + mention + system + user)
                .reply(reply)
                .mention(mention)
                .system(system)
                .user(user)
                .build();
    }
    /**
     * 初始化指定用户的未读数量。
     */

    public Long clearUnread(Integer userId, UnreadMessageType type, Long value) {
        if (value == null || value <= 0) {
            return getUnread(userId, type);
        }
        Long remaining = template.execute(
                DECREMENT_SCRIPT,
                java.util.List.of(buildKey(userId, type)),
                value.toString()
        );
        return remaining == null ? 0L : remaining;
    }
    /**
     * 清空指定会话的未读数量，并同步更新总未读数。
     */

    @Override
    public Long clearUnread(Integer userId, UnreadMessageType type) {
        template.delete(buildKey(userId, type));
        return 0L;
    }
    /**
     * 将指定用户的未读数量加一。
     */

    public long incrUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        Long increment = template.opsForValue().increment(key, value);
        return increment == null ? 0 : increment;
    }
    /**
     * 将指定用户的未读数量减一。
     */

    @Override
    public long addUnread(Integer userId, UnreadMessageType type, Long value) {
        if (value == null || value <= 0) {
            return getUnread(userId, type);
        }
        return incrUnread(userId, type, value);
    }
}
