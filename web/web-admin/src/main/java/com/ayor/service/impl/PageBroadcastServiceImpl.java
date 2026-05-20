package com.ayor.service.impl;

import com.ayor.entity.dto.PageBroadcastDTO;
import com.ayor.entity.message.PageBroadcastEventMessage;
import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.mapper.ThemeMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.PageBroadcastService;
import com.ayor.type.PageBroadcastEventType;
import com.ayor.type.PageBroadcastScopeType;
import com.ayor.type.PageBroadcastStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PageBroadcastServiceImpl implements PageBroadcastService {

    private static final String IDS_KEY = "page_broadcast:ids";

    private static final String ITEM_KEY_PREFIX = "page_broadcast:item:";

    private static final String SCOPE_KEY_PREFIX = "page_broadcast:scope:";

    private final StringRedisTemplate redisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final ThemeMapper themeMapper;

    private final TopicMapper topicMapper;

    private final ObjectMapper objectMapper;

    public PageBroadcastServiceImpl(StringRedisTemplate redisTemplate,
                                    RabbitTemplate rabbitTemplate,
                                    ThemeMapper themeMapper,
                                    TopicMapper topicMapper) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.themeMapper = themeMapper;
        this.topicMapper = topicMapper;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String createPageBroadcast(PageBroadcastDTO dto) {
        String validation = validateCreate(dto);
        if (validation != null) {
            return validation;
        }
        PageBroadcastVO vo = new PageBroadcastVO();
        LocalDateTime startTime = dto.getStartTime() == null ? LocalDateTime.now() : dto.getStartTime();
        vo.setBroadcastId(UUID.randomUUID().toString());
        vo.setScopeType(dto.getScopeType());
        vo.setScopeId(dto.getScopeId());
        vo.setContent(dto.getContent().trim());
        vo.setStartTime(startTime);
        vo.setEndTime(dto.getEndTime());
        vo.setStatus(resolveStatus(vo, LocalDateTime.now()));

        try {
            String json = objectMapper.writeValueAsString(vo);
            if (vo.getEndTime() == null) {
                redisTemplate.opsForValue().set(itemKey(vo.getBroadcastId()), json);
            } else {
                redisTemplate.opsForValue().set(itemKey(vo.getBroadcastId()), json, ttlMinutes(vo.getEndTime()), TimeUnit.MINUTES);
            }
            redisTemplate.opsForSet().add(IDS_KEY, vo.getBroadcastId());
            redisTemplate.opsForSet().add(scopeKey(vo.getScopeType(), vo.getScopeId()), vo.getBroadcastId());
            publishEvent(PageBroadcastEventType.CREATED, vo);
            return null;
        } catch (JsonProcessingException exception) {
            return "页面广播序列化失败";
        }
    }

    @Override
    public List<PageBroadcastVO> listPageBroadcasts() {
        Set<String> ids = redisTemplate.opsForSet().members(IDS_KEY);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        List<PageBroadcastVO> result = new ArrayList<>();
        for (String id : ids) {
            PageBroadcastVO vo = readBroadcast(id);
            if (vo == null) {
                redisTemplate.opsForSet().remove(IDS_KEY, id);
                continue;
            }
            vo.setStatus(resolveStatus(vo, now));
            result.add(vo);
        }
        result.sort(Comparator.comparing(PageBroadcastVO::getStartTime).reversed());
        return result;
    }

    @Override
    public String deletePageBroadcast(String broadcastId) {
        if (!StringUtils.hasText(broadcastId)) {
            return "页面广播不存在";
        }
        PageBroadcastVO vo = readBroadcast(broadcastId);
        if (vo == null) {
            redisTemplate.opsForSet().remove(IDS_KEY, broadcastId);
            return "页面广播不存在";
        }
        redisTemplate.delete(itemKey(broadcastId));
        redisTemplate.opsForSet().remove(IDS_KEY, broadcastId);
        redisTemplate.opsForSet().remove(scopeKey(vo.getScopeType(), vo.getScopeId()), broadcastId);
        publishEvent(PageBroadcastEventType.DELETED, vo);
        return null;
    }

    private String validateCreate(PageBroadcastDTO dto) {
        if (dto == null || dto.getScopeType() == null) {
            return "页面范围不能为空";
        }
        if (!StringUtils.hasText(dto.getContent())) {
            return "页面广播内容不能为空";
        }
        LocalDateTime startTime = dto.getStartTime() == null ? LocalDateTime.now() : dto.getStartTime();
        if (dto.getEndTime() != null && !dto.getEndTime().isAfter(startTime)) {
            return "结束时间必须晚于开始时间";
        }
        if (dto.getScopeType() == PageBroadcastScopeType.HOME) {
            return dto.getScopeId() == null ? null : "主页广播不能指定范围ID";
        }
        if (dto.getScopeId() == null || dto.getScopeId() <= 0) {
            return "范围ID不能为空";
        }
        if (dto.getScopeType() == PageBroadcastScopeType.THEME && !themeMapper.existsById(dto.getScopeId())) {
            return "主题不存在";
        }
        if (dto.getScopeType() == PageBroadcastScopeType.TOPIC && !topicMapper.existsById(dto.getScopeId())) {
            return "话题不存在";
        }
        return null;
    }

    private PageBroadcastVO readBroadcast(String broadcastId) {
        String json = redisTemplate.opsForValue().get(itemKey(broadcastId));
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PageBroadcastVO.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private PageBroadcastStatus resolveStatus(PageBroadcastVO vo, LocalDateTime now) {
        if (now.isBefore(vo.getStartTime())) {
            return PageBroadcastStatus.PENDING;
        }
        if (vo.getEndTime() != null && !now.isBefore(vo.getEndTime())) {
            return PageBroadcastStatus.EXPIRED;
        }
        return PageBroadcastStatus.ACTIVE;
    }

    private long ttlMinutes(LocalDateTime endTime) {
        long millis = Duration.between(LocalDateTime.now(), endTime).toMillis();
        return Math.max(1, (millis + 59_999) / 60_000);
    }

    private void publishEvent(PageBroadcastEventType eventType, PageBroadcastVO vo) {
        rabbitTemplate.convertAndSend(
                "page-broadcast.direct",
                "page-broadcast.changed",
                new PageBroadcastEventMessage(eventType, vo.getBroadcastId(), vo.getScopeType(), vo.getScopeId(), vo));
    }

    private String itemKey(String broadcastId) {
        return ITEM_KEY_PREFIX + broadcastId;
    }

    private String scopeKey(PageBroadcastScopeType scopeType, Integer scopeId) {
        if (scopeType == PageBroadcastScopeType.HOME) {
            return SCOPE_KEY_PREFIX + PageBroadcastScopeType.HOME;
        }
        return SCOPE_KEY_PREFIX + scopeType + ":" + scopeId;
    }
}
