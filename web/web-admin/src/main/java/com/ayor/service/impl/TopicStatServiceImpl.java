package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.service.TopicStatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TopicStatServiceImpl extends ServiceImpl<TopicStatMapper, TopicStat> implements TopicStatService {

    @Override
    public PageEntity<TopicStat> getTopicStats(Integer pageNum, Integer pageSize, Integer topicId) {
        Page<TopicStat> page = this.lambdaQuery()
                .eq(topicId != null, TopicStat::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String updateTopicStat(Integer statId, TopicStat topicStat) {
        if (statId == null) {
            return "统计记录不存在";
        }
        TopicStat exist = this.getById(statId);
        if (exist == null) {
            return "统计记录不存在";
        }
        if (topicStat.getTopicId() != null) {
            exist.setTopicId(topicStat.getTopicId());
        }
        if (topicStat.getThreadCount() != null) {
            exist.setThreadCount(topicStat.getThreadCount());
        }
        if (topicStat.getViewCount() != null) {
            exist.setViewCount(topicStat.getViewCount());
        }
        return this.updateById(exist) ? null : "更新统计失败";
    }
}
