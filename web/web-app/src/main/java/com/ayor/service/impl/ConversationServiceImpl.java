package com.ayor.service.impl;

import com.ayor.entity.cache.ConversationListCacheItem;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.AccountService;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.ConversationService;
import com.ayor.service.AuthorizationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

    private static final String CONVERSATION_CACHE = "conversation";

    private static final String CONVERSATION_LIST_CACHE = "conversationList";

    private final AccountMapper accountMapper;

    private final ChatUnreadService chatUnreadService;

    private final AuthorizationService authorizationService;

    private final AccountService accountService;

    private final CacheManager cacheManager;

    public static String conversationPairKey(Integer accountId, Integer toAccountId) {
        if (accountId == null || toAccountId == null) {
            return "";
        }
        int first = Math.min(accountId, toAccountId);
        int second = Math.max(accountId, toAccountId);
        return first + ":" + second;
    }

    /**
     * 获取当前用户与指定用户之间的会话信息。
     */

    @Override
    public ConversationVO getConversationByAccountId(Integer accountId, Integer toAccountId) {
        if (accountId == null) {
            return null;
        }
        authorizationService.assertCanStartConversation(accountId, toAccountId);

        String cacheKey = conversationPairKey(accountId, toAccountId);
        ConversationVO cachedConversation = getConversationCache().get(cacheKey, ConversationVO.class);
        if (cachedConversation != null) {
            return cachedConversation;
        }

        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, accountId)
                .eq(Conversation::getBetaAccountId, toAccountId)
                .or()
                .eq(Conversation::getAlphaAccountId, toAccountId)
                .eq(Conversation::getBetaAccountId, accountId)
                .one();
        if (conversation == null) {
            return null;
        }
        ConversationVO conversationVO = new ConversationVO();
        conversationVO.setConversationId(conversation.getConversationId());
        // 如果是发起者来查找对话
        if (accountId.equals(conversation.getAlphaAccountId()) ) {
            if(conversation.getHidden() == 1 ) {
                conversation.setHidden(0);
                this.updateById( conversation);
                return cacheConversation(cacheKey, conversationVO);
            }
                if (conversation.getHidden() == 3) {
                conversation.setHidden(2);
                this.updateById( conversation);
                return cacheConversation(cacheKey, conversationVO);
            }
            return cacheConversation(cacheKey, conversationVO);
        }
        // 如果是接收者来查找对话
        if (accountId.equals(conversation.getBetaAccountId())) {
            if (conversation.getHidden() == 2) {
                conversation.setHidden(0);
                this.updateById( conversation);
                return cacheConversation(cacheKey, conversationVO);
            }
            if (conversation.getHidden() == 3) {
                conversation.setHidden(1);
                this.updateById( conversation);
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
        boolean isConversationExists = baseMapper.existsConversationByUsers(fromAccount.getAccountId(), toAccount.getAccountId());
        if (isConversationExists) {
            return "已存在对话";
        }
        Conversation conversation = new Conversation();
        conversation.setCreateTime(new Date());
        conversation.setAlphaAccountId(fromAccount.getAccountId());
        conversation.setBetaAccountId(toAccount.getAccountId());
        conversation.setUpdateTime(new Date());
        if (!save(conversation)) {
            return "创建失败";
        }
        evictConversationCache(conversation);
        evictConversationListCache(fromAccount.getAccountId());
        evictConversationListCache(toAccount.getAccountId());
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
        return toConversationVOs(cacheItems);
    }

    private List<ConversationListCacheItem> buildConversationListCacheItems(Account account) {
        List<ConversationListCacheItem> cacheItems = new ArrayList<>();
        List<Conversation> initiativeConversations = this.baseMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
        );
        List<Conversation> reactiveConversations = this.baseMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
        );

        initiativeConversations.forEach(conversation -> {
            if (conversation.getHidden() != 1 && conversation.getHidden() != 3) {
                cacheItems.add(new ConversationListCacheItem(
                        conversation.getConversationId(),
                        conversation.getBetaAccountId(),
                        conversation.getUpdateTime()
                ));
            }
        });
        reactiveConversations.forEach(conversation -> {
            if (conversation.getHidden() != 2 && conversation.getHidden() != 3) {
                cacheItems.add(new ConversationListCacheItem(
                        conversation.getConversationId(),
                        conversation.getAlphaAccountId(),
                        conversation.getUpdateTime()
                ));
            }
        });
        return cacheItems;
    }

    private List<ConversationVO> toConversationVOs(List<ConversationListCacheItem> cacheItems) {
        List<ConversationVO> conversationVOs = new ArrayList<>();
        for (ConversationListCacheItem cacheItem : cacheItems) {
            conversationVOs.add(ConversationVO.builder()
                    .conversationId(cacheItem.getConversationId())
                    .userInfo(getConversationUserInfo(cacheItem.getPartnerAccountId()))
                    .updateTime(cacheItem.getUpdateTime())
                    .build());
        }
        return conversationVOs;
    }
    /**
     * 获取当前用户的未读会话摘要。
     */

    @Override
    public List<ChatUnread> getUnreadList(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        List<Conversation> alphCconversationList = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
                .list();
        List<Conversation> betaCconversationList = this.lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
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
        chatUnreadService.clearUnread(conversationId, fromUserId);
        return null;
    }
    private UserInfoVO getConversationUserInfo(Integer accountId) {
        UserInfoVO userInfo = accountService.getUserInfo(accountId);
        if (userInfo == null) {
            return null;
        }
        UserInfoVO conversationUserInfo = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, conversationUserInfo);
        conversationUserInfo.setPermission(null);
        return conversationUserInfo;
    }

    private ConversationVO cacheConversation(String cacheKey, ConversationVO conversationVO) {
        getConversationCache().put(cacheKey, conversationVO);
        return conversationVO;
    }

    private void evictConversationCache(Conversation conversation) {
        String cacheKey = conversationPairKey(conversation.getAlphaAccountId(), conversation.getBetaAccountId());
        getConversationCache().evict(cacheKey);
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


}
