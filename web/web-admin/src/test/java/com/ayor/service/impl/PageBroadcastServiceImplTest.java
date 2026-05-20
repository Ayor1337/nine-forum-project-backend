package com.ayor.service.impl;

import com.ayor.entity.dto.PageBroadcastDTO;
import com.ayor.entity.message.PageBroadcastEventMessage;
import com.ayor.mapper.ThemeMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.type.PageBroadcastScopeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageBroadcastServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ThemeMapper themeMapper;

    @Mock
    private TopicMapper topicMapper;

    @Test
    void shouldStoreTopicBroadcastAndPublishCreatedEvent() {
        PageBroadcastServiceImpl service = new PageBroadcastServiceImpl(
                redisTemplate,
                rabbitTemplate,
                themeMapper,
                topicMapper
        );
        PageBroadcastDTO dto = new PageBroadcastDTO();
        dto.setScopeType(PageBroadcastScopeType.TOPIC);
        dto.setScopeId(12);
        dto.setContent("话题提示");
        dto.setStartTime(LocalDateTime.now().minusMinutes(1));
        dto.setEndTime(LocalDateTime.now().plusMinutes(30));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(topicMapper.existsById(12)).thenReturn(true);

        String result = service.createPageBroadcast(dto);

        assertNull(result);
        verify(valueOperations).set(anyString(), anyString(), eq(30L), eq(TimeUnit.MINUTES));
        verify(setOperations).add(eq("page_broadcast:ids"), anyString());
        verify(setOperations).add(eq("page_broadcast:scope:TOPIC:12"), anyString());
        ArgumentCaptor<PageBroadcastEventMessage> captor = ArgumentCaptor.forClass(PageBroadcastEventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq("page-broadcast.direct"), eq("page-broadcast.changed"), captor.capture());
        assertEquals(PageBroadcastScopeType.TOPIC, captor.getValue().getScopeType());
        assertEquals(12, captor.getValue().getScopeId());
    }

    @Test
    void shouldStoreBroadcastWithoutTimeLimitWhenStartAndEndTimeAreMissing() {
        PageBroadcastServiceImpl service = new PageBroadcastServiceImpl(
                redisTemplate,
                rabbitTemplate,
                themeMapper,
                topicMapper
        );
        PageBroadcastDTO dto = new PageBroadcastDTO();
        dto.setScopeType(PageBroadcastScopeType.HOME);
        dto.setContent("长期提示");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        String result = service.createPageBroadcast(dto);

        assertNull(result);
        verify(valueOperations).set(anyString(), anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), eq(1L), eq(TimeUnit.MINUTES));
        verify(setOperations).add(eq("page_broadcast:scope:HOME"), anyString());
    }
}
