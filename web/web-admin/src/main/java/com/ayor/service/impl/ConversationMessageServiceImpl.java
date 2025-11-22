package com.ayor.service.impl;


import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.service.ConversationMessageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements ConversationMessageService {


    @Override
    public PageEntity<ConversationMessage> getMessages(Integer conversationId, Integer pageNum, Integer pageSize) {
        if (conversationId == null) {
            return null;
        }
        Page<ConversationMessage> page = this.lambdaQuery()
                .eq(ConversationMessage::getConversationId, conversationId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

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
}
