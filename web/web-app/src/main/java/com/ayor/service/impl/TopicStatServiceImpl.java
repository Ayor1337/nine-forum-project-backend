package com.ayor.service.impl;

import com.ayor.entity.pojo.TopicStat;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.service.TopicStatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TopicStatServiceImpl extends ServiceImpl<TopicStatMapper, TopicStat> implements TopicStatService {


    private final CacheManager cacheManager;

    @Override
    public void updateTopicStat() {
        Objects.requireNonNull(cacheManager.getCache("topicList")).clear();
        this.baseMapper.updateThreadCount();
        this.baseMapper.updateViewCount();
    }


}
