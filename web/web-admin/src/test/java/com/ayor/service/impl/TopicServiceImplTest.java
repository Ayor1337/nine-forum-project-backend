package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.pojo.Topic;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.TopicStatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private TopicStatMapper topicStatMapper;

    @Test
    void shouldRouteDataUrlCoverThroughSharedImageStorageService() {
        TopicServiceImpl service = new TopicServiceImpl(cacheManager, imageStorageService, topicStatMapper);
        TopicDTO dto = new TopicDTO();
        dto.setCoverUrl("data:image/png;base64,AAAA");
        Topic topic = new Topic();
        StoredImage stored = new StoredImage();
        stored.setUrl("forum/topic/cover.png");
        Base64Upload upload = new Base64Upload(dto.getCoverUrl(), "cover");
        when(imageStorageService.storeImageBase64Image(upload, "topic/")).thenReturn(stored);

        String result = ReflectionTestUtils.invokeMethod(service, "applyCoverUrl", dto, topic);

        assertNull(result);
        assertEquals("forum/topic/cover.png", topic.getCoverUrl());
        verify(imageStorageService).storeImageBase64Image(upload, "topic/");
    }

    @Test
    void shouldPreserveOrdinaryCoverUrlWithoutDecoding() {
        TopicServiceImpl service = new TopicServiceImpl(cacheManager, imageStorageService, topicStatMapper);
        TopicDTO dto = new TopicDTO();
        dto.setCoverUrl("forum/topic/existing.png");
        Topic topic = new Topic();

        String result = ReflectionTestUtils.invokeMethod(service, "applyCoverUrl", dto, topic);

        assertNull(result);
        assertEquals("forum/topic/existing.png", topic.getCoverUrl());
        verifyNoInteractions(imageStorageService);
    }
}
