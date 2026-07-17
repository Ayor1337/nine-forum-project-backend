package com.ayor.service.impl;

import com.ayor.entity.cache.ConversationListCacheItem;
import com.ayor.entity.pojo.ConversationUserSetting;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationUserSettingMapper;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.ConversationService;
import com.ayor.service.AuthorizationService;
import com.ayor.service.PresenceService;
import com.ayor.util.ConversationViewUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private static final String CONVERSATION_CACHE = "conversation";

    private static final String CONVERSATION_LIST_CACHE = "conversationList";

    private final AccountMapper accountMapper;

    private final ChatUnreadService chatUnreadService;

    private final AuthorizationService authorizationService;

    private final ConversationUserSettingMapper conversationUserSettingMapper;

    private final PresenceService presenceService;

    private final CacheManager cacheManager;

    private final ConversationViewUtils conversationViewUtils;

    private final SimpMessagingTemplate messagingTemplate;

    public static String conversationPairKey(Integer accountId, Integer toAccountId) {
        if (accountId == null || toAccountId == null) {
            return "";
        }
        int first = Math.min(accountId, toAccountId);
        int second = Math.max(accountId, toAccountId);
        return first + ":" + second;
    }

    public static String conversationViewerCacheKey(Integer viewerId, Integer accountId, Integer toAccountId) {
        return viewerId + ":" + conversationPairKey(accountId, toAccountId);
    }

    /**
     * 获取当前用户与指定用户之间的会话信息。
     */

    @Override
    public ConversationVO getConversationByAccountId(Integer accountId, Integer toAccountId) {
        if (accountId == null || toAccountId == null) {
            return null;
        }
        authorizationService.assertCanStartConversation(accountId, toAccountId);

        String cacheKey = conversationViewerCacheKey(accountId, accountId, toAccountId);
        ConversationVO cachedConversation = getConversationCache().get(cacheKey, ConversationVO.class);
        if (cachedConversation != null) {
            refreshViewerState(cachedConversation, accountId);
            return cachedConversation;
        }

        Conversation conversation = baseMapper.selectConversationByUsers(accountId, toAccountId);
        if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
            return null;
        }
        ConversationVO conversationVO = conversationViewUtils.toConversationVO(conversation, accountId);
        // 如果是发起者来查找对话
        if (accountId.equals(conversation.getAlphaAccountId()) ) {
            if(conversation.getHidden() == 1 ) {
                conversation.setHidden(0);
                this.updateById( conversation);
                afterConversationVisibilityRecovered(conversation, accountId);
                return cacheConversation(cacheKey, conversationVO);
            }
                if (conversation.getHidden() == 3) {
                conversation.setHidden(2);
                this.updateById( conversation);
                afterConversationVisibilityRecovered(conversation, accountId);
                return cacheConversation(cacheKey, conversationVO);
            }
            return cacheConversation(cacheKey, conversationVO);
        }
        // 如果是接收者来查找对话
        if (accountId.equals(conversation.getBetaAccountId())) {
            if (conversation.getHidden() == 2) {
                conversation.setHidden(0);
                this.updateById( conversation);
                afterConversationVisibilityRecovered(conversation, accountId);
                return cacheConversation(cacheKey, conversationVO);
            }
            if (conversation.getHidden() == 3) {
                conversation.setHidden(1);
                this.updateById( conversation);
                afterConversationVisibilityRecovered(conversation, accountId);
                return cacheConversation(cacheKey, conversationVO);
            }
            return cacheConversation(cacheKey, conversationVO);
        }
        return cacheConversation(cacheKey, conversationVO);
    }
    /**
     * 隐藏当前用户的会话。
     */

    @Override
    public String hiddenConversation(Integer conversationId, Integer accountId) {
        authorizationService.assertCanAccessConversation(accountId, conversationId);
        Conversation conversation = this.getById(conversationId);
        if (accountId == null) {
            return "用户不存在";
        }
        if (conversation == null) {
            return "对话不存在";
        }
        if (accountId.equals(conversation.getAlphaAccountId())) {
            if (conversation.getHidden() == 0) {
                conversation.setHidden(1);
            }
            if (conversation.getHidden() == 2) {
                conversation.setHidden(3);
            }
            this.updateById(conversation);
            evictConversationCache(conversation);
            evictConversationListCache(accountId);
            return null;
        }
        if (accountId.equals(conversation.getBetaAccountId())) {
            if (conversation.getHidden() == 0) {
                conversation.setHidden(2);
            }
            if (conversation.getHidden() == 1) {
                conversation.setHidden(3);
            }
            this.updateById(conversation);
            evictConversationCache(conversation);
            evictConversationListCache(accountId);
            return null;
        }
        return null;
    }
    /**
     * 创建新的私聊会话。
     */

    @Override
    public String createNewConversation(Integer accountId, String toUsername) {
        Account fromAccount = accountMapper.getAccountById(accountId);
        if (fromAccount == null) {
            return "发送用户不存在";
        }
        Account toAccount = accountMapper.getAccountByUsername(toUsername);
        if (toAccount == null) {
            return "接收用户不存在";
        }
        authorizationService.assertCanStartConversation(accountId, toAccount.getAccountId());
        Conversation existingConversation = baseMapper.selectConversationByUsers(fromAccount.getAccountId(), toAccount.getAccountId());
        if (existingConversation != null) {
            if (!Boolean.TRUE.equals(existingConversation.getIsDeleted())) {
                return "已存在对话";
            }
            existingConversation.setIsDeleted(false);
            existingConversation.setHidden(0);
            existingConversation.setUpdateTime(new Date());
            normalizeConversationPair(existingConversation);
            if (!updateById(existingConversation)) {
                return "创建失败";
            }
            evictConversationCache(existingConversation);
            evictConversationListCache(fromAccount.getAccountId());
            evictConversationListCache(toAccount.getAccountId());
            pushConversationToParticipants(existingConversation);
            return null;
        }
        Conversation conversation = new Conversation();
        Date now = new Date();
        conversation.setCreateTime(now);
        conversation.setAlphaAccountId(fromAccount.getAccountId());
        conversation.setBetaAccountId(toAccount.getAccountId());
        normalizeConversationPair(conversation);
        conversation.setUpdateTime(now);
        conversation.setIsDeleted(false);
        conversation.setHidden(0);
        if (!save(conversation)) {
            return "创建失败";
        }
        evictConversationCache(conversation);
        evictConversationListCache(fromAccount.getAccountId());
        evictConversationListCache(toAccount.getAccountId());
        pushConversationToParticipants(conversation);
        return null;
    }
    /**
     * 获取当前用户的会话列表。
     */

    @Override
    public List<ConversationVO> getConversationList(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        List<ConversationListCacheItem> cacheItems = getConversationListCacheItems(account.getAccountId());
        if (cacheItems == null) {
            cacheItems = buildConversationListCacheItems(account);
            putConversationListCache(account.getAccountId(), cacheItems);
        }
        return toConversationVOs(cacheItems, account.getAccountId());
    }

    private List<ConversationListCacheItem> buildConversationListCacheItems(Account account) {
        List<ConversationListCacheItem> cacheItems = new ArrayList<>();
        List<Conversation> initiativeConversations = this.baseMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
                .and(wrapper -> wrapper.eq(Conversation::getIsDeleted, false).or().isNull(Conversation::getIsDeleted))
        );
        List<Conversation> reactiveConversations = this.baseMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
                .and(wrapper -> wrapper.eq(Conversation::getIsDeleted, false).or().isNull(Conversation::getIsDeleted))
        );

        initiativeConversations.forEach(conversation -> {
            if (conversation.getHidden() != 1 && conversation.getHidden() != 3) {
                cacheItems.add(new ConversationListCacheItem(
                        conversation.getConversationId(),
                        account.getAccountId(),
                        conversation.getBetaAccountId(),
                        conversation.getUpdateTime(),
                        resolvePinned(conversation.getConversationId(), account.getAccountId())
                ));
            }
        });
        reactiveConversations.forEach(conversation -> {
            if (conversation.getHidden() != 2 && conversation.getHidden() != 3) {
                cacheItems.add(new ConversationListCacheItem(
                        conversation.getConversationId(),
                        account.getAccountId(),
                        conversation.getAlphaAccountId(),
                        conversation.getUpdateTime(),
                        resolvePinned(conversation.getConversationId(), account.getAccountId())
                ));
            }
        });
        sortConversationListCacheItems(cacheItems);
        return cacheItems;
    }

    private List<ConversationVO> toConversationVOs(List<ConversationListCacheItem> cacheItems, Integer viewerId) {
        List<ConversationVO> conversationVOs = new ArrayList<>();
        for (ConversationListCacheItem cacheItem : cacheItems) {
            Conversation conversation = this.getById(cacheItem.getConversationId());
            if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
                continue;
            }
            conversationVOs.add(conversationViewUtils.toConversationVO(
                    conversation,
                    cacheItem.getViewerAccountId() == null ? viewerId : cacheItem.getViewerAccountId(),
                    cacheItem.getPartnerAccountId()));
        }
        return conversationVOs;
    }

    @Override
    public ConversationVO pinConversation(Integer conversationId, Integer accountId, Boolean pinned) {
        authorizationService.assertCanAccessConversation(accountId, conversationId);
        Conversation conversation = this.getById(conversationId);
        if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
            return null;
        }
        ConversationUserSetting setting = conversationUserSettingMapper.selectOne(
                Wrappers.<ConversationUserSetting>lambdaQuery()
                        .eq(ConversationUserSetting::getConversationId, conversationId)
                        .eq(ConversationUserSetting::getAccountId, accountId)
                        .last("LIMIT 1"));
        Date now = new Date();
        if (setting == null) {
            setting = new ConversationUserSetting();
            setting.setConversationId(conversationId);
            setting.setAccountId(accountId);
            setting.setPinned(Boolean.TRUE.equals(pinned));
            setting.setCreateTime(now);
            setting.setUpdateTime(now);
            conversationUserSettingMapper.insert(setting);
        } else {
            setting.setPinned(Boolean.TRUE.equals(pinned));
            setting.setUpdateTime(now);
            conversationUserSettingMapper.updateById(setting);
        }
        evictConversationCache(conversation);
        evictConversationListCache(accountId);
        ConversationVO conversationVO = conversationViewUtils.toConversationVO(conversation, accountId);
        messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/conversations", conversationVO);
        return conversationVO;
    }
    /**
     * 获取当前用户的未读会话摘要。
     */

    @Override
    public List<ChatUnread> getUnreadList(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        List<Conversation> alphCconversationList = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
                .and(wrapper -> wrapper.eq(Conversation::getIsDeleted, false).or().isNull(Conversation::getIsDeleted))
                .list();
        List<Conversation> betaCconversationList = this.lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
                .and(wrapper -> wrapper.eq(Conversation::getIsDeleted, false).or().isNull(Conversation::getIsDeleted))
                .list();
        List<ChatUnread> chatUnreadList = new ArrayList<>();
        alphCconversationList.forEach(con -> {
            Long unread = chatUnreadService.getUnread(con.getConversationId(), accountId);
            chatUnreadList.add(ChatUnread.builder()
                    .conversationId(con.getConversationId())
                    .fromUserId(con.getBetaAccountId())
                    .unread(unread)
                    .build());
        });
        betaCconversationList.forEach(con -> {
            Long unread = chatUnreadService.getUnread(con.getConversationId(), accountId);
            chatUnreadList.add(ChatUnread.builder()
                    .conversationId(con.getConversationId())
                    .fromUserId(con.getAlphaAccountId())
                    .unread(unread)
                    .build());
        });
        return chatUnreadList;
    }
    /**
     * 清空指定会话的未读数量，并同步更新总未读数。
     */

    @Override
    public String clearUnread(Integer conversationId, Integer accountId, Integer fromUserId) {
        if (accountId == null || fromUserId == null) {
            return "无此用户";
        }
        authorizationService.assertCanClearConversationUnread(accountId, conversationId, fromUserId);
        chatUnreadService.clearUnreadAndTotal(conversationId, accountId);
        return null;
    }

    private ConversationVO cacheConversation(String cacheKey, ConversationVO conversationVO) {
        getConversationCache().put(cacheKey, conversationVO);
        return conversationVO;
    }

    private void evictConversationCache(Conversation conversation) {
        Cache cache = getConversationCache();
        if (cache == null) {
            return;
        }
        String pairKey = conversationPairKey(conversation.getAlphaAccountId(), conversation.getBetaAccountId());
        cache.evict(pairKey);
        cache.evict(conversationViewerCacheKey(
                conversation.getAlphaAccountId(),
                conversation.getAlphaAccountId(),
                conversation.getBetaAccountId()));
        cache.evict(conversationViewerCacheKey(
                conversation.getBetaAccountId(),
                conversation.getAlphaAccountId(),
                conversation.getBetaAccountId()));
    }

    private List<ConversationListCacheItem> getConversationListCacheItems(Integer accountId) {
        Cache cache = getConversationListCache();
        if (cache == null) {
            return null;
        }
        List<?> cachedItems = cache.get(accountId, List.class);
        if (cachedItems == null) {
            return null;
        }
        List<ConversationListCacheItem> cacheItems = new ArrayList<>();
        for (Object cachedItem : cachedItems) {
            if (cachedItem instanceof ConversationListCacheItem item) {
                cacheItems.add(item);
            } else {
                cache.evict(accountId);
                return null;
            }
        }
        return cacheItems;
    }

    private void putConversationListCache(Integer accountId, List<ConversationListCacheItem> cacheItems) {
        Cache cache = getConversationListCache();
        if (cache != null) {
            cache.put(accountId, cacheItems);
        }
    }

    private void evictConversationListCache(Integer accountId) {
        Cache cache = getConversationListCache();
        if (cache != null && accountId != null) {
            cache.evict(accountId);
        }
    }

    private Cache getConversationCache() {
        return cacheManager.getCache(CONVERSATION_CACHE);
    }

    private Cache getConversationListCache() {
        return cacheManager.getCache(CONVERSATION_LIST_CACHE);
    }

    private void afterConversationVisibilityRecovered(Conversation conversation, Integer viewerId) {
        evictConversationCache(conversation);
        evictConversationListCache(conversation.getAlphaAccountId());
        evictConversationListCache(conversation.getBetaAccountId());
        pushConversation(viewerId, conversation);
    }

    private void pushConversationToParticipants(Conversation conversation) {
        pushConversation(conversation.getAlphaAccountId(), conversation);
        pushConversation(conversation.getBetaAccountId(), conversation);
    }

    private void pushConversation(Integer viewerId, Conversation conversation) {
        if (viewerId == null) {
            return;
        }
        ConversationVO conversationVO = conversationViewUtils.toConversationVO(conversation, viewerId);
        messagingTemplate.convertAndSendToUser(viewerId.toString(), "/notif/conversations", conversationVO);
    }

    private Boolean resolvePinned(Integer conversationId, Integer accountId) {
        ConversationUserSetting setting = conversationUserSettingMapper.selectOne(
                Wrappers.<ConversationUserSetting>lambdaQuery()
                        .eq(ConversationUserSetting::getConversationId, conversationId)
                        .eq(ConversationUserSetting::getAccountId, accountId)
                        .last("LIMIT 1"));
        return setting != null && Boolean.TRUE.equals(setting.getPinned());
    }

    private void refreshViewerState(ConversationVO conversationVO, Integer viewerId) {
        if (conversationVO == null) {
            return;
        }
        conversationVO.setPinned(resolvePinned(conversationVO.getConversationId(), viewerId));
        if (conversationVO.getUserInfo() != null) {
            conversationVO.setPartnerOnline(presenceService.isOnline(conversationVO.getUserInfo().getAccountId()));
        }
    }

    private void sortConversationListCacheItems(List<ConversationListCacheItem> cacheItems) {
        cacheItems.sort(Comparator
                .comparing((ConversationListCacheItem item) -> Boolean.TRUE.equals(item.getPinned()))
                .reversed()
                .thenComparing(
                        ConversationListCacheItem::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private void normalizeConversationPair(Conversation conversation) {
        if (conversation == null
                || conversation.getAlphaAccountId() == null
                || conversation.getBetaAccountId() == null) {
            return;
        }
        int first = Math.min(conversation.getAlphaAccountId(), conversation.getBetaAccountId());
        int second = Math.max(conversation.getAlphaAccountId(), conversation.getBetaAccountId());
        conversation.setAlphaAccountId(first);
        conversation.setBetaAccountId(second);
    }

}
