package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Override
    public PageEntity<Conversation> getConversations(Integer pageNum, Integer pageSize, Integer alphaAccountId, Integer betaAccountId) {
        Page<Conversation> page = this.lambdaQuery()
                .eq(alphaAccountId != null, Conversation::getAlphaAccountId, alphaAccountId)
                .eq(betaAccountId != null, Conversation::getBetaAccountId, betaAccountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String deleteConversation(Integer conversationId) {
        if (conversationId == null) {
            return "会话不存在";
        }
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            return "会话不存在";
        }
        conversation.setIsDeleted(true);
        return this.updateById(conversation) ? null : "删除会话失败";
    }
}
