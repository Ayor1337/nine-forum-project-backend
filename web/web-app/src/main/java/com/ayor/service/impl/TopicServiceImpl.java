package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.dto.TopicDTO;
import com.ayor.entity.app.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private ThreaddMapper threaddMapper;

    @Resource
    private MinioService minioService;

    @Override
    public String getTopicNameById(Integer topicId) {
        Topic topic = this.lambdaQuery().eq(Topic::getTopicId, topicId).one();
        if (topic == null || topic.getIsDeleted()) {
            return null;
        }
        return topic.getTitle();
    }

    @Override
    public List<TopicVO> getTopicListByThemeId(Integer themeId) {
        List<Topic> topics = topicMapper.getTopicByThemeId(themeId);
        List<TopicVO> topicVOList = new ArrayList<>();
        topics.forEach(topic -> {
            if (!topic.getIsDeleted()) {
                TopicVO topicVO = new TopicVO();
                BeanUtils.copyProperties(topic, topicVO);
                topicVOList.add(topicVO);
            }
        });
        return topicVOList;
    }

    @Override
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
            String coverUrl = minioService.uploadBase64(cover, "topic/");
            topic.setCoverUrl(coverUrl);
        } catch (Exception e) {
            return "图片上传失败";
        }
        BeanUtils.copyProperties(topicDTO, topic);
        topic.setCreateTime(new Date());

        return this.save(topic) ? null : "添加失败, 未知异常";
    }

    @Override
    public String updateTopic(TopicDTO topicDTO) {
        if (topicDTO == null || topicDTO.getTitle().equals("待输入标题")) {
            return "请填写主题";
        }

        Topic topic = this.getById(topicDTO.getTopicId());
        BeanUtils.copyProperties(topicDTO, topic);

        if (!topicDTO.getCover().getBase64().startsWith("nineforum")) {
            try {
                String coverUrl = minioService.uploadBase64(topicDTO.getCover(), "topic/");
                topic.setCoverUrl(coverUrl);
            } catch (Exception e) {
                return "图片上传失败";
            }
        }
        return this.updateById( topic) ? null : "更新失败, 未知异常";
    }

    @Override
    public String deleteTopic(Integer topicId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            return "主题不存在";
        }
        topic.setIsDeleted(true);
        threaddMapper.deleteThreadByTopicId(topicId);
        return this.updateById(topic) ? null : "删除失败, 未知异常";
    }

}
