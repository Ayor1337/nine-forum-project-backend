package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.ConversationUserSetting;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.ConversationUserSettingMapper;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.AuthorizationService;
import com.ayor.entity.cache.ConversationListCacheItem;
import com.ayor.entity.vo.UserPermissionVO;
import com.ayor.service.PresenceService;
import com.ayor.util.ConversationViewUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private ConversationMapper conversationMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ConversationUserSettingMapper conversationUserSettingMapper;

    @Mock
    private PresenceService presenceService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache conversationCache;

    @Mock
    private Cache conversationListCache;

    @Mock
    private ConversationViewUtils conversationViewUtils;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    // 测试按账号获取会话时使用缓存管理器而非@Cacheable
    @Test
    void getConversationByAccountIdShouldUseCacheManagerInsteadOfCacheable() throws NoSuchMethodException {
        Method method = ConversationServiceImpl.class.getMethod(
                "getConversationByAccountId",
                Integer.class,
                Integer.class
        );

        assertNull(method.getAnnotation(org.springframework.cache.annotation.Cacheable.class));
    }

    // 测试会话配对键对双方账号顺序对称
    @Test
    void conversationPairKeyShouldBeSymmetric() {
        String first = ConversationServiceImpl.conversationPairKey(1, 2);
        String second = ConversationServiceImpl.conversationPairKey(2, 1);

        assertEquals(first, second);
    }

    // 测试返回缓存会话前先授权
    @Test
    void shouldAuthorizeBeforeReturningCachedConversation() {
        ConversationServiceImpl service = createService();
        ConversationVO cachedConversation = new ConversationVO();
        cachedConversation.setConversationId(9);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(conversationCache.get("1:1:2", ConversationVO.class)).thenReturn(cachedConversation);

        ConversationVO result = service.getConversationByAccountId(1, 2);

        assertSame(cachedConversation, result);
        inOrder(authorizationService, cacheManager).verify(authorizationService).assertCanStartConversation(1, 2);
        verify(cacheManager).getCache("conversation");
    }

    // 测试隐藏会话在授权失败时被拒绝
    @Test
    void shouldRejectHideConversationWhenAuthorizationFails() {
        ConversationServiceImpl service = createService();
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).assertCanAccessConversation(3, 7);

        assertThrows(AccessDeniedException.class, () -> service.hiddenConversation(7, 3));
    }

    // 测试授权未读清理带有会话上下文
    @Test
    void shouldAuthorizeUnreadClearingWithConversationContext() {
        ConversationServiceImpl service = createService();
        String result = service.clearUnread(7, 1, 2);

        assertNull(result);
        verify(authorizationService).assertCanClearConversationUnread(1, 7, 2);
        verify(chatUnreadService).clearUnreadAndTotal(7, 1);
    }

    // 测试新增会话前先授权创建
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
        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);

        String result = service.createNewConversation(1, "target");

        assertNull(result);
        verify(authorizationService).assertCanStartConversation(1, 2);
    }

    // 测试解析最新用户信息当会话列表元数据缓存命中
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
        Conversation conversation = new Conversation();
        conversation.setConversationId(9);
        conversation.setAlphaAccountId(1);
        conversation.setBetaAccountId(2);
        ConversationVO vo = ConversationVO.builder()
                .conversationId(9)
                .userInfo(cachedUserInfo)
                .updateTime(updateTime)
                .build();
        when(conversationMapper.selectById(9)).thenReturn(conversation);
        when(conversationViewUtils.toConversationVO(conversation, 1, 2)).thenReturn(vo);

        List<ConversationVO> result = service.getConversationList(1);

        assertEquals(1, result.size());
        assertEquals(9, result.get(0).getConversationId());
        assertEquals(updateTime, result.get(0).getUpdateTime());
        assertEquals("fresh-avatar.jpg", result.get(0).getUserInfo().getAvatarUrl());
        assertEquals(cachedUserInfo.getPermission(), result.get(0).getUserInfo().getPermission());
        verify(conversationMapper, never()).selectList(any());
    }

    // 测试清理双方参与者会话列表当创建会话
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
        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.createNewConversation(1, "target");

        assertNull(result);
        verify(conversationListCache).evict(1);
        verify(conversationListCache).evict(2);
    }

    // 测试创建会话时规范化双方账号顺序
    @Test
    void shouldNormalizeConversationPairWhenCreatingConversation() {
        ConversationServiceImpl service = createService();
        Account from = new Account();
        from.setAccountId(9);
        Account to = new Account();
        to.setAccountId(2);
        to.setUsername("target");
        when(accountMapper.getAccountById(9)).thenReturn(from);
        when(accountMapper.getAccountByUsername("target")).thenReturn(to);
        when(conversationMapper.selectConversationByUsers(9, 2)).thenReturn(null);
        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            assertEquals(2, conversation.getAlphaAccountId());
            assertEquals(9, conversation.getBetaAccountId());
            return 1;
        });
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);

        String result = service.createNewConversation(9, "target");

        assertNull(result);
        verify(authorizationService).assertCanStartConversation(9, 2);
    }

    // 测试软删除会话再次发起时恢复旧会话
    @Test
    void shouldRestoreSoftDeletedConversationWhenCreatingExistingPair() {
        ConversationServiceImpl service = createService();
        Account from = new Account();
        from.setAccountId(9);
        Account to = new Account();
        to.setAccountId(2);
        to.setUsername("target");
        Conversation deleted = new Conversation();
        deleted.setConversationId(7);
        deleted.setAlphaAccountId(9);
        deleted.setBetaAccountId(2);
        deleted.setHidden(3);
        deleted.setIsDeleted(true);
        when(accountMapper.getAccountById(9)).thenReturn(from);
        when(accountMapper.getAccountByUsername("target")).thenReturn(to);
        when(conversationMapper.selectConversationByUsers(9, 2)).thenReturn(deleted);
        when(conversationMapper.updateById(deleted)).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.createNewConversation(9, "target");

        assertNull(result);
        assertEquals(false, deleted.getIsDeleted());
        assertEquals(0, deleted.getHidden());
        assertEquals(2, deleted.getAlphaAccountId());
        assertEquals(9, deleted.getBetaAccountId());
        verify(conversationMapper, never()).insert(any(Conversation.class));
        verify(conversationListCache).evict(9);
        verify(conversationListCache).evict(2);
    }

    // 测试重建会话列表当缓存值使用旧 VO 格式
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
        ConversationVO conversationVO = ConversationVO.builder()
                .conversationId(9)
                .userInfo(partnerUserInfo)
                .build();
        when(conversationMapper.selectById(9)).thenReturn(conversation);
        when(conversationViewUtils.toConversationVO(conversation, 1, 2)).thenReturn(conversationVO);

        List<ConversationVO> result = service.getConversationList(1);

        assertEquals(1, result.size());
        assertEquals(9, result.get(0).getConversationId());
        verify(conversationListCache).evict(1);
        verify(conversationListCache).put(anyInt(), any());
    }

    // 测试置顶会话按当前用户维度保存并推送会话列表项
    @Test
    void shouldPinConversationForCurrentUserAndPushConversationUpdate() {
        ConversationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setConversationId(9);
        conversation.setAlphaAccountId(1);
        conversation.setBetaAccountId(2);
        conversation.setIsDeleted(false);
        ConversationVO conversationVO = ConversationVO.builder()
                .conversationId(9)
                .pinned(true)
                .build();

        when(conversationMapper.selectById(9)).thenReturn(conversation);
        when(conversationUserSettingMapper.selectOne(any())).thenReturn(null);
        when(conversationUserSettingMapper.insert(any(ConversationUserSetting.class))).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);
        when(conversationViewUtils.toConversationVO(conversation, 1)).thenReturn(conversationVO);

        ConversationVO result = service.pinConversation(9, 1, true);

        assertSame(conversationVO, result);
        verify(authorizationService).assertCanAccessConversation(1, 9);
        verify(conversationUserSettingMapper).insert(any(ConversationUserSetting.class));
        verify(conversationListCache).evict(1);
        verify(conversationCache).evict("1:2");
        verify(conversationCache).evict("1:1:2");
        verify(conversationCache).evict("2:1:2");
        verify(messagingTemplate).convertAndSendToUser("1", "/notif/conversations", conversationVO);
    }

    // 测试缓存会话命中时刷新动态置顶与在线状态
    @Test
    void shouldRefreshPinnedAndOnlineStateWhenConversationCacheHits() {
        ConversationServiceImpl service = createService();
        ConversationVO cachedConversation = new ConversationVO();
        cachedConversation.setConversationId(9);
        UserInfoVO partner = new UserInfoVO();
        partner.setAccountId(2);
        cachedConversation.setUserInfo(partner);
        ConversationUserSetting setting = new ConversationUserSetting();
        setting.setPinned(true);

        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(conversationCache.get("1:1:2", ConversationVO.class)).thenReturn(cachedConversation);
        when(conversationUserSettingMapper.selectOne(any())).thenReturn(setting);
        when(presenceService.isOnline(2)).thenReturn(true);

        ConversationVO result = service.getConversationByAccountId(1, 2);

        assertSame(cachedConversation, result);
        assertEquals(true, result.getPinned());
        assertEquals(true, result.getPartnerOnline());
    }

    // 测试隐藏会话恢复时只推送当前用户，不推送另一方
    @Test
    void shouldPushOnlyViewerWhenHiddenConversationVisibilityRecovers() {
        ConversationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setConversationId(9);
        conversation.setAlphaAccountId(1);
        conversation.setBetaAccountId(2);
        conversation.setHidden(3);
        conversation.setIsDeleted(false);
        ConversationVO conversationVO = ConversationVO.builder()
                .conversationId(9)
                .build();
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);
        when(conversationCache.get("1:1:2", ConversationVO.class)).thenReturn(null);
        when(conversationMapper.selectConversationByUsers(1, 2)).thenReturn(conversation);
        when(conversationMapper.updateById(conversation)).thenReturn(1);
        when(conversationViewUtils.toConversationVO(conversation, 1)).thenReturn(conversationVO);

        ConversationVO result = service.getConversationByAccountId(1, 2);

        assertSame(conversationVO, result);
        assertEquals(2, conversation.getHidden());
        verify(messagingTemplate).convertAndSendToUser("1", "/notif/conversations", conversationVO);
        verify(messagingTemplate, never()).convertAndSendToUser("2", "/notif/conversations", conversationVO);
        verify(conversationListCache).evict(1);
        verify(conversationListCache).evict(2);
    }

    private ConversationServiceImpl createService() {
        ConversationServiceImpl service = new ConversationServiceImpl(
                accountMapper,
                chatUnreadService,
                authorizationService,
                conversationUserSettingMapper,
                presenceService,
                cacheManager,
                conversationViewUtils,
                messagingTemplate
        );
        ReflectionTestUtils.setField(service, "baseMapper", conversationMapper);
        return service;
    }
}
