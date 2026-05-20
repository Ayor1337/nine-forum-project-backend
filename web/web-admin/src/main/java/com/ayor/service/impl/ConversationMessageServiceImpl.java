package com.ayor.service.impl;


import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements ConversationMessageService {


    /**
     * 分页查询某个会话下的消息记录。
     */
    @Override
    public PageEntity<ConversationMessageVO> getMessages(Integer conversationId, Integer pageNum, Integer pageSize) {
        if (conversationId == null) {
            return null;
        }
        Page<ConversationMessage> page = this.lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public ConversationMessageVO getMessageById(Integer messageId) {
        if (messageId == null) {
            return null;
        }
        return toVO(this.getById(messageId));
    }

    @Override
    public String createMessage(ConversationMessage message) {
        if (message == null || message.getConversationId() == null || message.getAccountId() == null) {
            return "消息参数不完整";
        }
        if (!StringUtils.hasText(message.getContent())) {
            return "消息内容不能为空";
        }
        Date now = new Date();
        if (message.getCreateTime() == null) {
            message.setCreateTime(now);
        }
        message.setUpdateTime(now);
        if (message.getIsDeleted() == null) {
            message.setIsDeleted(false);
        }
        if (message.getIsEdit() == null) {
            message.setIsEdit(false);
        }
        return this.save(message) ? null : "创建消息失败";
    }

    @Override
    public String updateMessage(ConversationMessage message) {
        if (message == null || message.getConversationMessageId() == null) {
            return "消息不存在";
        }
        ConversationMessage exist = this.getById(message.getConversationMessageId());
        if (exist == null) {
            return "消息不存在";
        }
        if (message.getConversationId() != null) {
            exist.setConversationId(message.getConversationId());
        }
        if (message.getAccountId() != null) {
            exist.setAccountId(message.getAccountId());
        }
        if (StringUtils.hasText(message.getContent())) {
            exist.setContent(message.getContent());
            exist.setIsEdit(true);
        }
        exist.setUpdateTime(new Date());
        return this.updateById(exist) ? null : "更新消息失败";
    }

    /**
     * 将消息标记为已删除，而不是直接物理移除。
     */
    @Override
    public String deleteMessage(Integer messageId) {
        if (messageId == null) {
            return "消息不存在";
        }
        ConversationMessage exist = this.getById(messageId);
        if (exist == null) {
            return "消息不存在";
        }
        exist.setIsDeleted(true);
        return this.updateById(exist) ? null : "删除消息失败";
    }

    private List<ConversationMessageVO> toVOList(List<ConversationMessage> messages) {
        List<ConversationMessageVO> conversationMessageVOS = new ArrayList<>();
        for (ConversationMessage message : messages) {
            conversationMessageVOS.add(toVO(message));
        }
        return conversationMessageVOS;
    }

    private ConversationMessageVO toVO(ConversationMessage message) {
        if (message == null) {
            return null;
        }
        ConversationMessageVO conversationMessageVO = new ConversationMessageVO();
        BeanUtils.copyProperties(message, conversationMessageVO);
        return conversationMessageVO;
    }
}
