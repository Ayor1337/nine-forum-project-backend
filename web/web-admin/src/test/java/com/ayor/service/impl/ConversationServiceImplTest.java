package com.ayor.service.impl;

import com.ayor.entity.pojo.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ConversationServiceImplTest {

    @Test
    void shouldEvictConversationCachesAfterDeletingConversation() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("conversation", "conversationList");
        cacheManager.getCache("conversation").put(5, "detail");
        cacheManager.getCache("conversationList").put(10, "alpha-list");
        cacheManager.getCache("conversationList").put(20, "beta-list");

        ConversationServiceImpl service = new ConversationServiceImpl(cacheManager) {
            @Override
            public Conversation getById(Serializable id) {
                return new Conversation(5, 10, 20, null, null, false, 0);
            }

            @Override
            public boolean updateById(Conversation entity) {
                return true;
            }
        };

        String result = service.deleteConversation(5);

        assertNull(result);
        assertNull(cacheManager.getCache("conversation").get(5));
        assertNull(cacheManager.getCache("conversationList").get(10));
        assertNull(cacheManager.getCache("conversationList").get(20));
    }
}
