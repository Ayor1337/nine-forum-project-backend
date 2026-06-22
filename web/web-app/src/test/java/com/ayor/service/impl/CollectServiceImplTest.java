package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CollectMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserRelationService;
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
class CollectServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private CollectMapper collectMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TipTapUtils tipTapUtils;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserRelationService userRelationService;

    @Test
    void shouldRejectCollectWhenBlockedWithThreadAuthor() {
        CollectServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(5);
        Threadd thread = new Threadd();
        thread.setThreadId(9);
        thread.setAccountId(11);
        thread.setIsDeleted(false);

        when(accountMapper.getAccountById(5)).thenReturn(account);
        when(threaddMapper.selectById(9)).thenReturn(thread);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(true);

        String result = service.insertCollect(5, 9);

        assertEquals("已拉黑，不能收藏", result);
        verify(collectMapper, never()).insert(any(Collect.class));
    }

    private CollectServiceImpl createService() {
        CollectServiceImpl service = new CollectServiceImpl(
                accountMapper,
                threaddMapper,
                tagMapper,
                tipTapUtils,
                privacyPolicyService,
                userRelationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", collectMapper);
        return service;
    }
}
