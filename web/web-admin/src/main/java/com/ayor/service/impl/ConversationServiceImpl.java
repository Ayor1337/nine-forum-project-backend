package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final CacheManager cacheManager;

    /**
     * 分页查询私信会话，可按双方账号 ID 组合过滤。
     */
    @Override
    public PageEntity<Conversation> getConversations(Integer pageNum, Integer pageSize, Integer alphaAccountId, Integer betaAccountId) {
        Page<Conversation> page = this.lambdaQuery()
                .eq(alphaAccountId != null, Conversation::getAlphaAccountId, alphaAccountId)
                .eq(betaAccountId != null, Conversation::getBetaAccountId, betaAccountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    /**
     * 将会话标记为已删除。
     */
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
        if (!this.updateById(conversation)) {
            return "删除会话失败";
        }
        evictConversationCaches(conversation);
        return null;
    }

    /**
     * 会话被后台删除后，需要清理会话详情和双方列表缓存。
     */
    private void evictConversationCaches(Conversation conversation) {
        evict("conversation", conversation.getConversationId());
        evict("conversationList", conversation.getAlphaAccountId());
        evict("conversationList", conversation.getBetaAccountId());
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
