package com.ayor.service.impl;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.PageBroadcastQueryService;
import com.ayor.type.PageBroadcastScopeType;
import com.ayor.type.PageBroadcastStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class PageBroadcastQueryServiceImpl implements PageBroadcastQueryService {

    private static final String ITEM_KEY_PREFIX = "page_broadcast:item:";

    private static final String SCOPE_KEY_PREFIX = "page_broadcast:scope:";

    private final StringRedisTemplate redisTemplate;

    private final TopicMapper topicMapper;

    private final ObjectMapper objectMapper;

    public PageBroadcastQueryServiceImpl(StringRedisTemplate redisTemplate, TopicMapper topicMapper) {
        this.redisTemplate = redisTemplate;
        this.topicMapper = topicMapper;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public List<PageBroadcastVO> listActiveBroadcasts(PageBroadcastScopeType scopeType, Integer scopeId) {
        if (scopeType == null) {
            return List.of();
        }
        List<String> scopeKeys = resolveScopeKeys(scopeType, scopeId);
        LocalDateTime now = LocalDateTime.now();
        List<PageBroadcastVO> broadcasts = new ArrayList<>();
        for (String scopeKey : scopeKeys) {
            Set<String> ids = redisTemplate.opsForSet().members(scopeKey);
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            for (String id : ids) {
                PageBroadcastVO vo = readBroadcast(id);
                if (vo == null) {
                    redisTemplate.opsForSet().remove(scopeKey, id);
                    continue;
                }
                PageBroadcastStatus status = resolveStatus(vo, now);
                if (status == PageBroadcastStatus.ACTIVE) {
                    vo.setStatus(status);
                    broadcasts.add(vo);
                }
            }
        }
        broadcasts.sort(Comparator.comparing(PageBroadcastVO::getStartTime));
        return broadcasts;
    }

    private List<String> resolveScopeKeys(PageBroadcastScopeType scopeType, Integer scopeId) {
        List<String> keys = new ArrayList<>();
        keys.add(scopeKey(PageBroadcastScopeType.HOME, null));
        if (scopeType == PageBroadcastScopeType.HOME) {
            return keys;
        }
        if (scopeType == PageBroadcastScopeType.THEME && scopeId != null) {
            keys.add(scopeKey(PageBroadcastScopeType.THEME, scopeId));
            return keys;
        }
        if (scopeType == PageBroadcastScopeType.TOPIC && scopeId != null) {
            Integer themeId = topicMapper.getThemeIdByTopicId(scopeId);
            if (themeId != null) {
                keys.add(scopeKey(PageBroadcastScopeType.THEME, themeId));
            }
            keys.add(scopeKey(PageBroadcastScopeType.TOPIC, scopeId));
        }
        return keys;
    }

    private PageBroadcastVO readBroadcast(String broadcastId) {
        String json = redisTemplate.opsForValue().get(ITEM_KEY_PREFIX + broadcastId);
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
        if (!now.isBefore(vo.getEndTime())) {
            return PageBroadcastStatus.EXPIRED;
        }
        return PageBroadcastStatus.ACTIVE;
    }

    private String scopeKey(PageBroadcastScopeType scopeType, Integer scopeId) {
        if (scopeType == PageBroadcastScopeType.HOME) {
            return SCOPE_KEY_PREFIX + PageBroadcastScopeType.HOME;
        }
        return SCOPE_KEY_PREFIX + scopeType + ":" + scopeId;
    }
}
