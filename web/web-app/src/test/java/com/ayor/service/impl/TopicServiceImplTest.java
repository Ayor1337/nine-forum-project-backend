package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.pojo.Topic;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.minio.MinioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private MinioService minioService;

    @Mock
    private TopicStatMapper topicStatMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Test
    void shouldUploadNewTopicCoverThroughImageStorageService() throws Exception {
        TopicServiceImpl service = spy(createService());
        TopicDTO topicDTO = new TopicDTO(null, "话题", new Base64Upload("data:image/png;base64,abc", "cover.png"), "描述", 3);
        StoredImage storedImage = createStoredImage("nineforum/topic/cover.png");

        when(imageStorageService.storeImageBase64Image(topicDTO.getCover(), "topic/")).thenReturn(storedImage);
        doAnswer(invocation -> {
            Topic topic = invocation.getArgument(0);
            topic.setTopicId(12);
            return true;
        }).when(service).save(any(Topic.class));
        when(topicStatMapper.initializeNewTopicStat(12)).thenReturn(1);

        String result = service.insertTopic(topicDTO);

        assertNull(result);
        verify(imageStorageService).storeImageBase64Image(topicDTO.getCover(), "topic/");
        verify(minioService, never()).uploadBase64(topicDTO.getCover(), "topic/");
    }

    @Test
    void shouldUploadUpdatedTopicCoverThroughImageStorageService() throws Exception {
        TopicServiceImpl service = spy(createService());
        Topic topic = new Topic();
        topic.setTopicId(9);
        topic.setCoverUrl("nineforum/topic/old.png");
        TopicDTO topicDTO = new TopicDTO(9, "话题", new Base64Upload("data:image/png;base64,abc", "cover.png"), "描述", 3);
        StoredImage storedImage = createStoredImage("nineforum/topic/new.png");

        when(service.getById(9)).thenReturn(topic);
        when(imageStorageService.storeImageBase64Image(topicDTO.getCover(), "topic/")).thenReturn(storedImage);
        when(service.updateById(topic)).thenReturn(true);

        String result = service.updateTopic(topicDTO);

        assertNull(result);
        verify(imageStorageService).storeImageBase64Image(topicDTO.getCover(), "topic/");
        verify(minioService, never()).uploadBase64(topicDTO.getCover(), "topic/");
    }

    private TopicServiceImpl createService() {
        TopicServiceImpl service = new TopicServiceImpl(
                topicMapper,
                threaddMapper,
                topicStatMapper,
                imageStorageService
        );
        ReflectionTestUtils.setField(service, "baseMapper", topicMapper);
        return service;
    }

    private StoredImage createStoredImage(String url) {
        StoredImage image = new StoredImage();
        image.setUrl(url);
        image.setObjectName("unused");
        image.setOriginalExt("png");
        image.setOutputExt("png");
        image.setMimeType("image/png");
        image.setFileSize(123L);
        image.setWidth(16);
        image.setHeight(16);
        image.setSha256("hash");
        image.setBytes(new byte[]{1, 2, 3});
        return image;
    }
}
