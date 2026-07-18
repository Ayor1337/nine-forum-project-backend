package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.image.ImageStorageService;
import com.ayor.entity.pojo.Topic;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.mapper.TopicStatMapper;
import com.ayor.service.TopicService;
import com.ayor.service.CacheInvalidationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分区服务实现
 */


@Service
@Transactional
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    private final TopicMapper topicMapper;

    private final ThreaddMapper threaddMapper;

    private final TopicStatMapper topicStatMapper;

    private final ImageStorageService imageStorageService;

    private final CacheInvalidationService cacheInvalidationService;
    /**
     * 根据主题 ID 查询主题名称，用于缓存和面包屑展示。
     */

    @Override
    @Cacheable(value = "topicName", key = "#topicId", condition = "#topicId != null", unless = "#result == null")
    public String getTopicNameById(Integer topicId) {
        Topic topic = this.lambdaQuery().eq(Topic::getTopicId, topicId).one();
        if (topic == null || topic.getIsDeleted()) {
            return null;
        }
        return topic.getTitle();
    }
    /**
     * 获取某个主题下的话题列表，并补充统计信息。
     */

    @Override
    @Cacheable(value = "topicList", key = "#themeId", condition = "#themeId != null", unless = "#result == null")
    public List<TopicVO> getTopicListByThemeId(Integer themeId) {
        List<Topic> topics = topicMapper.getTopicByThemeId(themeId);
        List<TopicVO> topicVOList = new ArrayList<>();
        topics.forEach(topic -> {
            if (!topic.getIsDeleted()) {
                TopicVO topicVO = new TopicVO();
                TopicStat topicStat = topicStatMapper.selectByTopicId(topic.getTopicId());
                BeanUtils.copyProperties(topic, topicVO);
                if (topicStat != null) {
                    topicVO.setThreadCount(topicStat.getThreadCount());
                    topicVO.setViewCount(topicStat.getViewCount());
                }
                topicVOList.add(topicVO);
            }
        });
        return topicVOList;
    }

    @Override
    /**
     * 创建新话题，上传封面并初始化话题统计。
     */
    public String insertTopic(TopicDTO topicDTO) {
        if (topicDTO == null || topicDTO.getTitle().equals("待输入标题")) {
            return "请填写主题";
        }
        if (topicDTO.getDescription().equals("待输入描述")) {
            topicDTO.setDescription(null);
        }
        Topic topic = new Topic();
        Base64Upload cover = topicDTO.getCover();
        try {
            topic.setCoverUrl(imageStorageService.storeImageBase64Image(cover, "topic/").getUrl());
        } catch (RuntimeException e) {
            return "图片上传失败";
        }
        BeanUtils.copyProperties(topicDTO, topic);
        topic.setCreateTime(new Date());
        this.save(topic);
        cacheInvalidationService.evict("topicList", topicDTO.getThemeId());
        cacheInvalidationService.evict("themeTopicList", "all");

        if (topicStatMapper.initializeNewTopicStat(topic.getTopicId()) <= 0) {
            return "添加失败, 未知异常";
        }
        return null;
    }

    @Override
    /**
     * 更新话题信息，必要时同步更新封面。
     */
    public String updateTopic(TopicDTO topicDTO) {
        if (topicDTO == null || topicDTO.getTitle().equals("待输入标题")) {
            return "请填写主题";
        }

        Topic topic = this.getById(topicDTO.getTopicId());
        if (topic == null) {
            return "主题不存在";
        }
        Integer oldThemeId = topic.getThemeId();
        BeanUtils.copyProperties(topicDTO, topic);

        if (!topicDTO.getCover().getBase64().startsWith("nineforum")) {
            try {
                topic.setCoverUrl(imageStorageService.storeImageBase64Image(topicDTO.getCover(), "topic/").getUrl());
            } catch (RuntimeException e) {
                return "图片上传失败";
            }
        }
        if (!this.updateById(topic)) {
            return "更新失败, 未知异常";
        }
        cacheInvalidationService.evict("topicName", topicDTO.getTopicId());
        cacheInvalidationService.evict("topicList", oldThemeId);
        if (!java.util.Objects.equals(oldThemeId, topicDTO.getThemeId())) {
            cacheInvalidationService.evict("topicList", topicDTO.getThemeId());
        }
        cacheInvalidationService.evict("themeTopicList", "all");
        return null;
    }

    @Override
    /**
     * 删除话题，并同步删除该话题下的帖子。
     */
    public String deleteTopic(Integer topicId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return "主题不存在";
        }
        Integer themeId = topic.getThemeId();
        Integer deletedThreads = threaddMapper.deleteThreadByTopicId(topicId);
        if (!this.removeByIdLogical(topicId)) {
            if (deletedThreads != null && deletedThreads > 0) {
                cacheInvalidationService.clearThreadRanking();
            }
            return "删除失败, 未知异常";
        }
        cacheInvalidationService.evict("topicName", topicId);
        cacheInvalidationService.evict("topicList", themeId);
        cacheInvalidationService.evict("themeTopicList", "all");
        cacheInvalidationService.clearThreadRanking();
        return null;
    }
    /**
     * 逻辑删除话题，并同步删除该话题下的帖子。
     */



    public String deleteTopicLogical(Integer topicId) {
        return deleteTopic(topicId);
    }
    /**
     * 将帖子标记为逻辑删除。
     */

    private boolean removeByIdLogical(Serializable topicId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return false;
        }
        topic.setIsDeleted(true);
        return this.updateById(topic);
    }

}
