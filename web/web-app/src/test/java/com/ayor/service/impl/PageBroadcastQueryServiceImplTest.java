package com.ayor.service.impl;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.mapper.TopicMapper;
import com.ayor.type.PageBroadcastScopeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageBroadcastQueryServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private TopicMapper topicMapper;

    @Test
    void shouldReturnHomeThemeAndTopicBroadcastsForTopicPage() {
        PageBroadcastQueryServiceImpl service = new PageBroadcastQueryServiceImpl(redisTemplate, topicMapper);
        LocalDateTime now = LocalDateTime.now();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(topicMapper.getThemeIdByTopicId(7)).thenReturn(3);
        when(setOperations.members("page_broadcast:scope:HOME")).thenReturn(java.util.Set.of("home"));
        when(setOperations.members("page_broadcast:scope:THEME:3")).thenReturn(java.util.Set.of("theme"));
        when(setOperations.members("page_broadcast:scope:TOPIC:7")).thenReturn(java.util.Set.of("topic"));
        when(valueOperations.get("page_broadcast:item:home")).thenReturn(json("home", "HOME", null, now));
        when(valueOperations.get("page_broadcast:item:theme")).thenReturn(json("theme", "THEME", 3, now));
        when(valueOperations.get("page_broadcast:item:topic")).thenReturn(json("topic", "TOPIC", 7, now));

        List<PageBroadcastVO> result = service.listActiveBroadcasts(PageBroadcastScopeType.TOPIC, 7);

        assertEquals(List.of("主页", "主题", "话题"), result.stream().map(PageBroadcastVO::getContent).toList());
    }

    private String json(String id, String scopeType, Integer scopeId, LocalDateTime now) {
        return """
                {"broadcastId":"%s","scopeType":"%s","scopeId":%s,"content":"%s","startTime":"%s","endTime":"%s"}
                """.formatted(
                id,
                scopeType,
                scopeId == null ? "null" : scopeId.toString(),
                switch (id) {
                    case "home" -> "主页";
                    case "theme" -> "主题";
                    default -> "话题";
                },
                now.minusMinutes(1),
                now.plusMinutes(10)
        );
    }
}
