package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.TopicService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

    private final CacheManager cacheManager;

    /**
     * 分页查询全部话题，并转换为管理端展示对象。
     */
    @Override
    public PageEntity<TopicVO> getTopics(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1)
            return null;
        Page<Topic> page = this.page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    /**
     * 按主题分页查询话题，用于主题下的话题管理页面。
     */
    @Override
    public PageEntity<TopicVO> getTopicsByThemeId(Integer themeId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1 || themeId == null)
            return null;
        Page<Topic> page = this.lambdaQuery()
                .eq(Topic::getThemeId, themeId)
                .page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    /**
     * 获取话题下拉选项，最多返回最近创建的 10 条匹配结果。
     */
    @Override
    public List<TopicVO> getTopicsAsOptions(String query) {
        List<Topic> topics = this.lambdaQuery()
                .like(Topic::getTitle, query)
                .orderByDesc(Topic::getCreateTime)
                .last("limit 10")
                .list();
        return toVOList(topics);
    }

    /**
     * 创建话题时补齐创建时间并初始化删除标记。
     */
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
        if (!this.save(topic)) {
            return "创建话题失败";
        }
        evictTopicCaches(null, null, topic.getThemeId());
        return null;
    }

    /**
     * 更新话题信息，保留原始创建时间。
     */
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
        Integer originalThemeId = topic.getThemeId();
        BeanUtils.copyProperties(topicDTO, topic);
        if (topicDTO.getCreateTime() == null) {
            topic.setCreateTime(originalCreateTime);
        }
        if (!this.updateById(topic)) {
            return "更新话题失败";
        }
        evictTopicCaches(topic.getTopicId(), originalThemeId, topic.getThemeId());
        return null;
    }

    /**
     * 逻辑删除话题。
     */
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
        if (!this.updateById(topic)) {
            return "删除话题失败";
        }
        evictTopicCaches(topicId, topic.getThemeId(), null);
        return null;
    }

    /**
     * 将话题实体列表转换成管理端展示对象列表。
     */
    private List<TopicVO> toVOList(List<Topic> topicList) {
        List<TopicVO> topicVOList = new ArrayList<>();
        topicList.forEach(topic -> {
            TopicVO topicVO = new TopicVO();
            BeanUtils.copyProperties(topic, topicVO);
            topicVOList.add(topicVO);
        });
        return topicVOList;
    }

    /**
     * 后台改动话题后同步清理前台读取缓存，避免列表和主题聚合数据脏读。
     */
    private void evictTopicCaches(Integer topicId, Integer originalThemeId, Integer currentThemeId) {
        if (topicId != null) {
            evict("topicName", topicId);
        }
        if (originalThemeId != null) {
            evict("topicList", originalThemeId);
        }
        if (currentThemeId != null) {
            evict("topicList", currentThemeId);
        }
        evict("themeTopicList", "all");
        evict("themeList", "all");
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

}
