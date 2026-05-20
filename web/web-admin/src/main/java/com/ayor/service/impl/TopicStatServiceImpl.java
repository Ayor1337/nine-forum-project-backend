package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.entity.vo.TopicStatVO;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.service.TopicStatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class TopicStatServiceImpl extends ServiceImpl<TopicStatMapper, TopicStat> implements TopicStatService {

    /**
     * 分页查询话题统计数据，可按话题 ID 过滤。
     */
    @Override
    public PageEntity<TopicStatVO> getTopicStats(Integer pageNum, Integer pageSize, Integer topicId) {
        Page<TopicStat> page = this.lambdaQuery()
                .eq(topicId != null, TopicStat::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public TopicStatVO getTopicStatById(Integer statId) {
        if (statId == null) {
            return null;
        }
        return toVO(this.getById(statId));
    }

    @Override
    public String createTopicStat(TopicStat topicStat) {
        if (topicStat == null || topicStat.getTopicId() == null) {
            return "话题不存在";
        }
        return this.save(topicStat) ? null : "创建统计失败";
    }

    /**
     * 更新话题统计记录中已填写的字段。
     */
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

    @Override
    public String deleteTopicStat(Integer statId) {
        if (statId == null) {
            return "统计记录不存在";
        }
        return this.removeById(statId) ? null : "删除统计失败";
    }

    private List<TopicStatVO> toVOList(List<TopicStat> topicStats) {
        List<TopicStatVO> topicStatVOS = new ArrayList<>();
        for (TopicStat topicStat : topicStats) {
            topicStatVOS.add(toVO(topicStat));
        }
        return topicStatVOS;
    }

    private TopicStatVO toVO(TopicStat topicStat) {
        if (topicStat == null) {
            return null;
        }
        TopicStatVO topicStatVO = new TopicStatVO();
        BeanUtils.copyProperties(topicStat, topicStatVO);
        return topicStatVO;
    }
}
