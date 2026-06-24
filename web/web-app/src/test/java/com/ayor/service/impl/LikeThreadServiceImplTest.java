package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.LikeThreadMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserRelationService;
import com.ayor.service.CacheInvalidationService;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeThreadServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private LikeThreadMapper likeThreadMapper;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    @Test
    void shouldRejectLikeWhenBlockedWithThreadAuthor() {
        LikeThreadServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(5);
        Threadd thread = new Threadd();
        thread.setThreadId(9);
        thread.setAccountId(11);
        thread.setIsDeleted(false);

        when(accountMapper.getAccountById(5)).thenReturn(account);
        when(threaddMapper.selectById(9)).thenReturn(thread);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(true);

        String result = service.insertLikeThreadId(5, 9);

        assertEquals("已拉黑，不能点赞", result);
        verify(likeThreadMapper, never()).insert(any(LikeThread.class));
    }

    private LikeThreadServiceImpl createService() {
        LikeThreadServiceImpl service = new LikeThreadServiceImpl(
                accountMapper,
                threaddMapper,
                new TipTapUtils(),
                privacyPolicyService,
                userRelationService,
                cacheInvalidationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", likeThreadMapper);
        return service;
    }
}
