package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.TopicDTO;
import com.ayor.entity.admin.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.TopicService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {


    @Override
    public PageEntity<TopicVO> getTopics(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1)
            return null;
        Page<Topic> page = this.page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public PageEntity<TopicVO> getTopicsByThemeId(Integer themeId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1 || themeId == null)
            return null;
        Page<Topic> page = this.lambdaQuery()
                .eq(Topic::getThemeId, themeId)
                .page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public List<TopicVO> getTopicsAsOptions(String query) {
        List<Topic> topics = this.lambdaQuery()
                .like(Topic::getTitle, query)
                .orderByDesc(Topic::getCreateTime)
                .last("limit 10")
                .list();
        return toVOList(topics);
    }

    @Override
    public String createTopic(TopicDTO topicDTO) {
        if (topicDTO == null || !StringUtils.hasText(topicDTO.getTitle())) {
            return "话题标题不能为空";
        }
        Topic topic = new Topic();
        BeanUtils.copyProperties(topicDTO, topic);
        if (topic.getCreateTime() == null) {
            topic.setCreateTime(new Date());
        }
        topic.setIsDeleted(false);
        return this.save(topic) ? null : "创建话题失败";
    }

    @Override
    public String updateTopic(TopicDTO topicDTO) {
        if (topicDTO == null || topicDTO.getTopicId() == null) {
            return "话题不存在";
        }
        Topic topic = this.getById(topicDTO.getTopicId());
        if (topic == null) {
            return "话题不存在";
        }
        Date originalCreateTime = topic.getCreateTime();
        BeanUtils.copyProperties(topicDTO, topic);
        if (topicDTO.getCreateTime() == null) {
            topic.setCreateTime(originalCreateTime);
        }
        return this.updateById(topic) ? null : "更新话题失败";
    }

    @Override
    public String deleteTopic(Integer topicId) {
        if (topicId == null) {
            return "话题不存在";
        }
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return "话题不存在";
        }
        topic.setIsDeleted(true);
        return this.updateById(topic) ? null : "删除话题失败";
    }

    private List<TopicVO> toVOList(List<Topic> topicList) {
        List<TopicVO> topicVOList = new ArrayList<>();
        topicList.forEach(topic -> {
            TopicVO topicVO = new TopicVO();
            BeanUtils.copyProperties(topic, topicVO);
            topicVOList.add(topicVO);
        });
        return topicVOList;
    }

}
