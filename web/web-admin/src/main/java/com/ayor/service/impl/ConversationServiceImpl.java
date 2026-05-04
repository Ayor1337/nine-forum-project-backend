package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final CacheManager cacheManager;

    /**
     * 分页查询私信会话，可按双方账号 ID 组合过滤。
     */
    @Override
    public PageEntity<ConversationVO> getConversations(Integer pageNum, Integer pageSize, Integer alphaAccountId, Integer betaAccountId) {
        Page<Conversation> page = this.lambdaQuery()
                .eq(alphaAccountId != null, Conversation::getAlphaAccountId, alphaAccountId)
                .eq(betaAccountId != null, Conversation::getBetaAccountId, betaAccountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public ConversationVO getConversationById(Integer conversationId) {
        if (conversationId == null) {
            return null;
        }
        return toVO(this.getById(conversationId));
    }

    @Override
    public String createConversation(Conversation conversation) {
        if (conversation == null || conversation.getAlphaAccountId() == null || conversation.getBetaAccountId() == null) {
            return "会话参数不完整";
        }
        Date now = new Date();
        if (conversation.getCreateTime() == null) {
            conversation.setCreateTime(now);
        }
        conversation.setUpdateTime(now);
        if (conversation.getIsDeleted() == null) {
            conversation.setIsDeleted(false);
        }
        if (conversation.getHidden() == null) {
            conversation.setHidden(0);
        }
        if (!this.save(conversation)) {
            return "创建会话失败";
        }
        evictConversationCaches(conversation);
        return null;
    }

    @Override
    public String updateConversation(Conversation conversation) {
        if (conversation == null || conversation.getConversationId() == null) {
            return "会话不存在";
        }
        Conversation exist = this.getById(conversation.getConversationId());
        if (exist == null) {
            return "会话不存在";
        }
        Integer oldAlphaAccountId = exist.getAlphaAccountId();
        Integer oldBetaAccountId = exist.getBetaAccountId();
        if (conversation.getAlphaAccountId() != null) {
            exist.setAlphaAccountId(conversation.getAlphaAccountId());
        }
        if (conversation.getBetaAccountId() != null) {
            exist.setBetaAccountId(conversation.getBetaAccountId());
        }
        if (conversation.getHidden() != null) {
            exist.setHidden(conversation.getHidden());
        }
        if (conversation.getIsDeleted() != null) {
            exist.setIsDeleted(conversation.getIsDeleted());
        }
        exist.setUpdateTime(new Date());
        if (!this.updateById(exist)) {
            return "更新会话失败";
        }
        evictConversationCaches(new Conversation(exist.getConversationId(), oldAlphaAccountId, oldBetaAccountId, exist.getCreateTime(), exist.getUpdateTime(), exist.getIsDeleted(), exist.getHidden()));
        evictConversationCaches(exist);
        return null;
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
        if (conversation == null) {
            return;
        }
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

    private List<ConversationVO> toVOList(List<Conversation> conversations) {
        List<ConversationVO> conversationVOS = new ArrayList<>();
        for (Conversation conversation : conversations) {
            conversationVOS.add(toVO(conversation));
        }
        return conversationVOS;
    }

    private ConversationVO toVO(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        ConversationVO conversationVO = new ConversationVO();
        BeanUtils.copyProperties(conversation, conversationVO);
        return conversationVO;
    }
}
