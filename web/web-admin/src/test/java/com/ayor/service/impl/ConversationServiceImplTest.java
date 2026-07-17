package com.ayor.service.impl;

import com.ayor.entity.pojo.Conversation;
import com.ayor.mapper.ConversationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache conversationCache;

    @Mock
    private Cache conversationListCache;

    @Test
    void shouldNormalizeConversationPairWhenCreatingConversation() {
        ConversationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setAlphaAccountId(9);
        conversation.setBetaAccountId(2);
        when(conversationMapper.selectConversationByUsers(2, 9)).thenReturn(null);
        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation saved = invocation.getArgument(0);
            assertThat(saved.getAlphaAccountId()).isEqualTo(2);
            assertThat(saved.getBetaAccountId()).isEqualTo(9);
            return 1;
        });

        String result = service.createConversation(conversation);

        assertThat(result).isNull();
    }

    @Test
    void shouldRestoreSoftDeletedConversationWhenCreatingExistingPair() {
        ConversationServiceImpl service = createService();
        Conversation request = new Conversation();
        request.setAlphaAccountId(9);
        request.setBetaAccountId(2);
        Conversation existing = new Conversation();
        existing.setConversationId(7);
        existing.setAlphaAccountId(2);
        existing.setBetaAccountId(9);
        existing.setHidden(3);
        existing.setIsDeleted(true);
        when(conversationMapper.selectConversationByUsers(2, 9)).thenReturn(existing);
        when(conversationMapper.updateById(existing)).thenReturn(1);
        when(cacheManager.getCache("conversation")).thenReturn(conversationCache);
        when(cacheManager.getCache("conversationList")).thenReturn(conversationListCache);

        String result = service.createConversation(request);

        assertThat(result).isNull();
        assertThat(existing.getIsDeleted()).isFalse();
        assertThat(existing.getHidden()).isZero();
        verify(conversationMapper, never()).insert(any(Conversation.class));
        verify(conversationCache).evict(7);
        verify(conversationListCache).evict(2);
        verify(conversationListCache).evict(9);
    }

    @Test
    void shouldNormalizeConversationPairWhenUpdatingConversation() {
        ConversationServiceImpl service = createService();
        Conversation existing = new Conversation();
        existing.setConversationId(7);
        existing.setAlphaAccountId(1);
        existing.setBetaAccountId(2);
        Conversation patch = new Conversation();
        patch.setConversationId(7);
        patch.setAlphaAccountId(9);
        patch.setBetaAccountId(2);
        when(conversationMapper.selectById(7)).thenReturn(existing);
        when(conversationMapper.updateById(existing)).thenReturn(1);

        String result = service.updateConversation(patch);

        assertThat(result).isNull();
        assertThat(existing.getAlphaAccountId()).isEqualTo(2);
        assertThat(existing.getBetaAccountId()).isEqualTo(9);
    }

    private ConversationServiceImpl createService() {
        ConversationServiceImpl service = new ConversationServiceImpl(cacheManager);
        ReflectionTestUtils.setField(service, "baseMapper", conversationMapper);
        return service;
    }
}
