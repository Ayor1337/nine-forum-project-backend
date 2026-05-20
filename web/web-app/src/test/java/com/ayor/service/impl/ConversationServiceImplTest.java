package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.AccountService;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.AuthorizationService;
import com.ayor.entity.cache.ConversationListCacheItem;
import com.ayor.entity.vo.UserPermissionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ChatUnreadService chatUnreadService;

    @Mock
    private AccountService accountService;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache conversationCache;

    @Mock
    private Cache conversationListCache;

    @Test
    void getConversationByAccountIdShouldUseCacheManagerInsteadOfCacheable() throws NoSuchMethodException {
        Method method = ConversationServiceImpl.class.getMethod(
                "getConversationByAccountId",
                Integer.class,
                Integer.class
        );

        assertNull(method.getAnnotation(org.springframework.cache.annotation.Cacheable.class));
    }

    @Test
    void conversationPairKeyShouldBeSymmetric() {
        String first = ConversationServiceImpl.conversationPairKey(1, 2);
        String second = ConversationServiceImpl.conversationPairKey(2, 1);

        assertEquals(first, second);
    }

    @Test
    void shouldAuthorizeBeforeReturningCachedConversation() {
        ConversationServiceImpl service = createService();
        ConversationVO cachedConversation = new ConversationVO();
        cachedConversation.setConversationId(9);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(conversationCache.get("1:2", ConversationVO.class)).thenReturn(cachedConversation);

        ConversationVO result = service.getConversationByAccountId(1, 2);

        assertSame(cachedConversation, result);
        inOrder(authorizationService, cacheManager).verify(authorizationService).assertCanStartConversation(1, 2);
        verify(cacheManager).getCache("conversation");
    }

    @Test
    void shouldRejectHideConversationWhenAuthorizationFails() {
        ConversationServiceImpl service = createService();
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).assertCanAccessConversation(3, 7);

        assertThrows(AccessDeniedException.class, () -> service.hiddenConversation(7, 3));
    }

    @Test
    void shouldAuthorizeUnreadClearingWithConversationContext() {
        ConversationServiceImpl service = createService();
        String result = service.clearUnread(7, 1, 2);

        assertNull(result);
        verify(authorizationService).assertCanClearConversationUnread(1, 7, 2);
        verify(chatUnreadService).clearUnread(7, 2);
    }

    @Test
    void shouldAuthorizeConversationCreationBeforeInsert() {
        ConversationServiceImpl service = createService();
        Account from = new Account();
        from.setAccountId(1);
        Account to = new Account();
        to.setAccountId(2);
        to.setUsername("target");
        when(accountMapper.getAccountById(1)).thenReturn(from);
        when(accountMapper.getAccountByUsername("target")).thenReturn(to);
        when(conversationMapper.existsConversationByUsers(anyInt(), anyInt())).thenReturn(false);
        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);

        String result = service.createNewConversation(1, "target");

        assertNull(result);
        verify(authorizationService).assertCanStartConversation(1, 2);
    }

    @Test
    void shouldResolveFreshUserInfoWhenConversationListMetadataCacheHits() {
        ConversationServiceImpl service = createService();
        Account viewer = new Account();
        viewer.setAccountId(1);
        Date updateTime = new Date();
        UserInfoVO cachedUserInfo = new UserInfoVO();
        cachedUserInfo.setAccountId(2);
        cachedUserInfo.setAvatarUrl("fresh-avatar.jpg");
        cachedUserInfo.setPermission(new UserPermissionVO());

        when(accountMapper.getAccountById(1)).thenReturn(viewer);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);
        when(conversationListCache.get(1, List.class))
                .thenReturn(List.of(new ConversationListCacheItem(9, 2, updateTime)));
        when(accountService.getUserInfo(2)).thenReturn(cachedUserInfo);

        List<ConversationVO> result = service.getConversationList(1);

        assertEquals(1, result.size());
        assertEquals(9, result.get(0).getConversationId());
        assertEquals(updateTime, result.get(0).getUpdateTime());
        assertEquals("fresh-avatar.jpg", result.get(0).getUserInfo().getAvatarUrl());
        assertNull(result.get(0).getUserInfo().getPermission());
        verify(accountService).getUserInfo(2);
        verify(conversationMapper, never()).selectList(any());
    }

    @Test
    void shouldEvictBothParticipantsConversationListWhenCreatingConversation() {
        ConversationServiceImpl service = createService();
        Account from = new Account();
        from.setAccountId(1);
        Account to = new Account();
        to.setAccountId(2);
        to.setUsername("target");
        when(accountMapper.getAccountById(1)).thenReturn(from);
        when(accountMapper.getAccountByUsername("target")).thenReturn(to);
        when(conversationMapper.existsConversationByUsers(anyInt(), anyInt())).thenReturn(false);
        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.createNewConversation(1, "target");

        assertNull(result);
        verify(conversationListCache).evict(1);
        verify(conversationListCache).evict(2);
    }

    @Test
    void shouldRebuildConversationListWhenCachedValueUsesOldVoFormat() {
        ConversationServiceImpl service = createService();
        Account viewer = new Account();
        viewer.setAccountId(1);
        Conversation conversation = new Conversation();
        conversation.setConversationId(9);
        conversation.setAlphaAccountId(1);
        conversation.setBetaAccountId(2);
        conversation.setHidden(0);
        conversation.setUpdateTime(new Date());
        UserInfoVO partnerUserInfo = new UserInfoVO();
        partnerUserInfo.setAccountId(2);

        when(accountMapper.getAccountById(1)).thenReturn(viewer);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);
        when(conversationListCache.get(1, List.class)).thenReturn(List.of(new ConversationVO()));
        when(conversationMapper.selectList(any())).thenReturn(List.of(conversation), List.of());
        when(accountService.getUserInfo(2)).thenReturn(partnerUserInfo);

        List<ConversationVO> result = service.getConversationList(1);

        assertEquals(1, result.size());
        assertEquals(9, result.get(0).getConversationId());
        verify(conversationListCache).evict(1);
        verify(conversationListCache).put(anyInt(), any());
    }

    private ConversationServiceImpl createService() {
        ConversationServiceImpl service = new ConversationServiceImpl(
                accountMapper,
                chatUnreadService,
                authorizationService,
                accountService,
                cacheManager
        );
        ReflectionTestUtils.setField(service, "baseMapper", conversationMapper);
        return service;
    }
}
