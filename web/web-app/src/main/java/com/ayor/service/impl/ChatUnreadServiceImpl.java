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
     * 构造 Redis 中使用的 key。
     */


    private String buildKey(Integer conversationId, Integer fromUserId) {
        return "chat:unread:" + fromUserId + ":" + conversationId;
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
    public Long getUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        String value = Optional.ofNullable(template.opsForValue().get(key))
                .orElse("0");
        return Long.parseLong(value);
    }
    /**
     * 初始化指定用户的未读数量。
     */

    public void newUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        template.opsForValue().set(key, "1");
    }
    /**
     * 清空指定会话的未读数量，并同步更新总未读数。
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
     * 将指定用户的未读数量加一。
     */

    public long incrUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        Long increment = template.opsForValue().increment(key);
        return increment == null ? 0 : increment;
    }
    /**
     * 将指定用户的未读数量减一。
     */

    public void decrUnread(Integer conversationId, Integer fromUserId) {
        String key = buildKey(conversationId, fromUserId);
        template.opsForValue().decrement(key);
    }
    /**
     * 按指定值增加未读数量，并返回最新结果。
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
