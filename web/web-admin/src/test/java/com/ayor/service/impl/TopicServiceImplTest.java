package com.ayor.service.impl;

import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.pojo.Topic;
import com.ayor.entity.vo.TopicVO;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.io.Serializable;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TopicServiceImplTest {

    @Test
    void shouldReturnTopicVoWhenTopicExists() {
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager()) {
            @Override
            public Topic getById(Serializable id) {
                return new Topic(5, "Java", "cover", "desc", new Date(), 2, false);
            }
        };

        TopicVO result = service.getTopicById(5);

        assertNotNull(result);
        assertEquals(5, result.getTopicId());
        assertEquals("Java", result.getTitle());
        assertEquals("cover", result.getCoverUrl());
    }

    @Test
    void shouldEvictRelatedCachesAfterCreatingTopic() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("topicName", "topicList", "themeTopicList", "themeList");
        cacheManager.getCache("topicList").put(3, "stale");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");

        TopicServiceImpl service = new TopicServiceImpl(cacheManager) {
            @Override
            public boolean save(Topic entity) {
                entity.setTopicId(11);
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("新话题");
        topicDTO.setThemeId(3);

        String result = service.createTopic(topicDTO);

        assertNull(result);
        assertNull(cacheManager.getCache("topicList").get(3));
        assertNull(cacheManager.getCache("themeTopicList").get("all"));
        assertNull(cacheManager.getCache("themeList").get("all"));
    }

    @Test
    void shouldEvictOldAndNewThemeCachesAfterUpdatingTopicTheme() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("topicName", "topicList", "themeTopicList", "themeList");
        cacheManager.getCache("topicName").put(9, "stale");
        cacheManager.getCache("topicList").put(1, "old-theme");
        cacheManager.getCache("topicList").put(2, "new-theme");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");

        TopicServiceImpl service = new TopicServiceImpl(cacheManager) {
            @Override
            public Topic getById(Serializable id) {
                return new Topic(9, "旧话题", null, null, new Date(), 1, false);
            }

            @Override
            public boolean updateById(Topic entity) {
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTopicId(9);
        topicDTO.setTitle("新话题");
        topicDTO.setThemeId(2);

        String result = service.updateTopic(topicDTO);

        assertNull(result);
        assertNull(cacheManager.getCache("topicName").get(9));
        assertNull(cacheManager.getCache("topicList").get(1));
        assertNull(cacheManager.getCache("topicList").get(2));
        assertNull(cacheManager.getCache("themeTopicList").get("all"));
        assertNull(cacheManager.getCache("themeList").get("all"));
    }

    @Test
    void shouldEvictConversationListsAfterDeletingTopic() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("topicName", "topicList", "themeTopicList", "themeList");
        cacheManager.getCache("topicName").put(7, "stale");
        cacheManager.getCache("topicList").put(4, "stale");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");

        TopicServiceImpl service = new TopicServiceImpl(cacheManager) {
            @Override
            public Topic getById(Serializable id) {
                return new Topic(7, "待删除", null, null, new Date(), 4, false);
            }

            @Override
            public boolean updateById(Topic entity) {
                return true;
            }
        };

        String result = service.deleteTopic(7);

        assertNull(result);
        assertNull(cacheManager.getCache("topicName").get(7));
        assertNull(cacheManager.getCache("topicList").get(4));
        assertNull(cacheManager.getCache("themeTopicList").get("all"));
        assertNull(cacheManager.getCache("themeList").get("all"));
    }
}
