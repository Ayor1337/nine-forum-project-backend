package com.ayor.service.impl;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
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

    private boolean existValue(String key) {
        return template.hasKey(key);
    }
    /**
     * 获取指定用户的未读数量。
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

    public void newUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        template.opsForValue().set(key, value.toString());
    }
    /**
     * 清空指定会话的未读数量，并同步更新总未读数。
     */

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

    public void decrUnread(Integer userId, UnreadMessageType type, Long value) {
        String key = buildKey(userId, type);
        template.opsForValue().decrement(key, value);
    }
    /**
     * 按指定值增加未读数量，并返回最新结果。
     */


    @Override
    public long addUnread(Integer userId, UnreadMessageType type, Long value) {
        if (getUnread(userId, type) == 0) {
            newUnread(userId, type, value);
            return 1;
        }
        return incrUnread(userId, type, 1L);
    }
}
