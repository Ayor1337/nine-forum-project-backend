package com.ayor.service.impl;

import com.ayor.service.ChatUnreadService;
import com.ayor.type.UnreadMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 聊天未读服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatUnreadServiceImpl implements ChatUnreadService {

    private static final DefaultRedisScript<String> ADD_UNREAD_SCRIPT = new DefaultRedisScript<>(
            "local conversation = redis.call('INCRBY', KEYS[1], 1); " +
                    "local total = redis.call('INCRBY', KEYS[2], 1); " +
                    "return tostring(conversation) .. ':' .. tostring(total);",
            String.class
    );

    private static final DefaultRedisScript<String> CLEAR_UNREAD_SCRIPT = new DefaultRedisScript<>(
            "local consumed = tonumber(redis.call('GET', KEYS[1]) or '0'); " +
                    "redis.call('DEL', KEYS[1]); " +
                    "local total = tonumber(redis.call('GET', KEYS[2]) or '0'); " +
                    "local remaining = total - consumed; " +
                    "if remaining <= 0 then redis.call('DEL', KEYS[2]); remaining = 0; " +
                    "else redis.call('SET', KEYS[2], remaining); end; " +
                    "return tostring(consumed) .. ':' .. tostring(remaining);",
            String.class
    );

    private final StringRedisTemplate template;
    /**
     * 构造 Redis 中使用的 key。
     */


    private String buildKey(Integer conversationId, Integer fromUserId) {
        return "chat:unread:" + fromUserId + ":" + conversationId;
    }
    /**
     * 判断指定 Redis key 是否存在。
     */

    private String buildTotalKey(Integer userId) {
        return "message:" + UnreadMessageType.USER_MESSAGE.getType() + ":unread:" + userId;
    }
    /**
     * 获取指定用户的未读数量。
     */

    @Override
    public Long getUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }
    /**
     * 初始化指定用户的未读数量。
     */

    public long clearUnread(Integer conversationId, Integer fromUserId) {
        return clearUnreadAndTotal(conversationId, fromUserId).conversationUnread();
    }
    /**
     * 按指定值增加未读数量，并返回最新结果。
     */


    @Override
    public long addUnread(Integer conversationId, Integer fromUserId) {
        return addUnreadAndTotal(conversationId, fromUserId).conversationUnread();
    }

    @Override
    public UnreadCounts addUnreadAndTotal(Integer conversationId, Integer userId) {
        String result = template.execute(
                ADD_UNREAD_SCRIPT,
                List.of(buildKey(conversationId, userId), buildTotalKey(userId))
        );
        return parseCounts(result);
    }

    @Override
    public UnreadCounts clearUnreadAndTotal(Integer conversationId, Integer userId) {
        String result = template.execute(
                CLEAR_UNREAD_SCRIPT,
                List.of(buildKey(conversationId, userId), buildTotalKey(userId))
        );
        return parseCounts(result);
    }

    private UnreadCounts parseCounts(String value) {
        if (value == null || value.isBlank()) {
            return new UnreadCounts(0, 0);
        }
        String[] values = value.split(":", 2);
        return new UnreadCounts(Long.parseLong(values[0]), Long.parseLong(values[1]));
    }
}
