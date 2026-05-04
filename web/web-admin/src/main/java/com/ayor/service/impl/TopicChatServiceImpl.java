package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.ayor.entity.vo.TopicChatVO;
import com.ayor.mapper.TopicChatMapper;
import com.ayor.service.TopicChatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class TopicChatServiceImpl extends ServiceImpl<TopicChatMapper, TopicChat> implements TopicChatService {

    /**
     * 分页查询话题聊天记录，可按话题过滤。
     */
    @Override
    public PageEntity<TopicChatVO> getTopicChats(Integer topicId, Integer pageNum, Integer pageSize) {
        Page<TopicChat> page = this.lambdaQuery()
                .eq(topicId != null, TopicChat::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public TopicChatVO getTopicChatById(Integer topicChatId) {
        if (topicChatId == null) {
            return null;
        }
        return toVO(this.getById(topicChatId));
    }

    @Override
    public String createTopicChat(TopicChat topicChat) {
        if (topicChat == null || topicChat.getTopicId() == null || topicChat.getAccountId() == null) {
            return "聊天记录参数不完整";
        }
        if (!StringUtils.hasText(topicChat.getContent())) {
            return "聊天内容不能为空";
        }
        if (topicChat.getCreateTime() == null) {
            topicChat.setCreateTime(new Date());
        }
        return this.save(topicChat) ? null : "创建聊天记录失败";
    }

    @Override
    public String updateTopicChat(TopicChat topicChat) {
        if (topicChat == null || topicChat.getTopicChatId() == null) {
            return "聊天记录不存在";
        }
        TopicChat exist = this.getById(topicChat.getTopicChatId());
        if (exist == null) {
            return "聊天记录不存在";
        }
        if (topicChat.getTopicId() != null) {
            exist.setTopicId(topicChat.getTopicId());
        }
        if (topicChat.getAccountId() != null) {
            exist.setAccountId(topicChat.getAccountId());
        }
        if (StringUtils.hasText(topicChat.getContent())) {
            exist.setContent(topicChat.getContent());
        }
        if (topicChat.getCreateTime() != null) {
            exist.setCreateTime(topicChat.getCreateTime());
        }
        return this.updateById(exist) ? null : "更新聊天记录失败";
    }

    /**
     * 删除指定的话题聊天记录。
     */
    @Override
    public String deleteTopicChat(Integer topicChatId) {
        if (topicChatId == null) {
            return "聊天记录不存在";
        }
        return this.removeById(topicChatId) ? null : "删除聊天记录失败";
    }

    private List<TopicChatVO> toVOList(List<TopicChat> topicChats) {
        List<TopicChatVO> topicChatVOS = new ArrayList<>();
        for (TopicChat topicChat : topicChats) {
            topicChatVOS.add(toVO(topicChat));
        }
        return topicChatVOS;
    }

    private TopicChatVO toVO(TopicChat topicChat) {
        if (topicChat == null) {
            return null;
        }
        TopicChatVO topicChatVO = new TopicChatVO();
        BeanUtils.copyProperties(topicChat, topicChatVO);
        return topicChatVO;
    }
}
