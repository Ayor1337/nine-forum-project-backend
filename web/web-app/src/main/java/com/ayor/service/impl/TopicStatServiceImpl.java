package com.ayor.service.impl;

import com.ayor.entity.pojo.TopicStat;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.service.TopicStatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TopicStatServiceImpl extends ServiceImpl<TopicStatMapper, TopicStat> implements TopicStatService {

    @Override
    public void updateTopicStat() {
        baseMapper.updateThreadCount();
        baseMapper.updateViewCount();
    }


}
