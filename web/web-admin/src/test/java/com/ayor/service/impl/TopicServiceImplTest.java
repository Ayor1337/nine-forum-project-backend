package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.pojo.Topic;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.entity.vo.TopicVO;
import com.ayor.minio.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TopicServiceImplTest {

    @Test
    void shouldReturnTopicVoWhenTopicExists() {
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager(), new MinioService(), null) {
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

        TopicStatMapper topicStatMapper = (TopicStatMapper) Proxy.newProxyInstance(
                TopicStatMapper.class.getClassLoader(),
                new Class[]{TopicStatMapper.class},
                (proxy, method, args) -> {
                    if ("initializeNewTopicStat".equals(method.getName())) {
                        assertEquals(11, args[0]);
                        return 1;
                    }
                    return null;
                }
        );
        TopicServiceImpl service = new TopicServiceImpl(cacheManager, new MinioService(), topicStatMapper) {
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
    void shouldUploadCoverWhenCreatingTopicWithBase64CoverUrl() {
        TopicStatMapper topicStatMapper = (TopicStatMapper) Proxy.newProxyInstance(
                TopicStatMapper.class.getClassLoader(),
                new Class[]{TopicStatMapper.class},
                (proxy, method, args) -> {
                    if ("initializeNewTopicStat".equals(method.getName())) {
                        assertEquals(12, args[0]);
                        return 1;
                    }
                    return null;
                }
        );
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager(), new MinioService() {
            @Override
            public String uploadBase64(Base64Upload dto, String path) {
                assertEquals("topic/", path);
                assertTrue(dto.getBase64().startsWith("data:image/"));
                return "nineforum/topic/cover.png";
            }
        }, topicStatMapper) {
            @Override
            public boolean save(Topic entity) {
                assertEquals("nineforum/topic/cover.png", entity.getCoverUrl());
                entity.setTopicId(12);
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("新话题");
        topicDTO.setThemeId(3);
        topicDTO.setCoverUrl("data:image/png;base64,AAAA");

        assertNull(service.createTopic(topicDTO));
    }

    @Test
    void shouldEvictOldAndNewThemeCachesAfterUpdatingTopicTheme() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("topicName", "topicList", "themeTopicList", "themeList");
        cacheManager.getCache("topicName").put(9, "stale");
        cacheManager.getCache("topicList").put(1, "old-theme");
        cacheManager.getCache("topicList").put(2, "new-theme");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");

        TopicServiceImpl service = new TopicServiceImpl(cacheManager, new MinioService(), null) {
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
    void shouldUploadCoverWhenUpdatingTopicWithBase64CoverUrl() {
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager(), new MinioService() {
            @Override
            public String uploadBase64(Base64Upload dto, String path) {
                assertEquals("topic/", path);
                return "nineforum/topic/new-cover.png";
            }
        }, null) {
            @Override
            public Topic getById(Serializable id) {
                return new Topic(9, "旧话题", "old-cover", null, new Date(), 1, false);
            }

            @Override
            public boolean updateById(Topic entity) {
                assertEquals("nineforum/topic/new-cover.png", entity.getCoverUrl());
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTopicId(9);
        topicDTO.setTitle("新话题");
        topicDTO.setThemeId(1);
        topicDTO.setCoverUrl("data:image/png;base64,BBBB");

        assertNull(service.updateTopic(topicDTO));
    }

    @Test
    void shouldKeepExistingCoverUrlWhenUpdatingTopicWithoutBase64() {
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager(), new MinioService() {
            @Override
            public String uploadBase64(Base64Upload dto, String path) {
                fail("已有封面地址不应重复上传");
                return null;
            }
        }, null) {
            @Override
            public Topic getById(Serializable id) {
                return new Topic(9, "旧话题", "old-cover", null, new Date(), 1, false);
            }

            @Override
            public boolean updateById(Topic entity) {
                assertEquals("nineforum/topic/existing.png", entity.getCoverUrl());
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTopicId(9);
        topicDTO.setTitle("新话题");
        topicDTO.setThemeId(1);
        topicDTO.setCoverUrl("nineforum/topic/existing.png");

        assertNull(service.updateTopic(topicDTO));
    }

    @Test
    void shouldReturnErrorWhenTopicStatInitializationFails() {
        TopicStatMapper topicStatMapper = (TopicStatMapper) Proxy.newProxyInstance(
                TopicStatMapper.class.getClassLoader(),
                new Class[]{TopicStatMapper.class},
                (proxy, method, args) -> "initializeNewTopicStat".equals(method.getName()) ? 0 : null
        );
        TopicServiceImpl service = new TopicServiceImpl(new ConcurrentMapCacheManager(), new MinioService(), topicStatMapper) {
            @Override
            public boolean save(Topic entity) {
                entity.setTopicId(13);
                return true;
            }
        };

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("统计失败");
        topicDTO.setThemeId(3);

        assertEquals("初始化话题统计失败", service.createTopic(topicDTO));
    }

    @Test
    void shouldEvictConversationListsAfterDeletingTopic() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("topicName", "topicList", "themeTopicList", "themeList");
        cacheManager.getCache("topicName").put(7, "stale");
        cacheManager.getCache("topicList").put(4, "stale");
        cacheManager.getCache("themeTopicList").put("all", "stale");
        cacheManager.getCache("themeList").put("all", "stale");

        TopicServiceImpl service = new TopicServiceImpl(cacheManager, new MinioService(), null) {
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
