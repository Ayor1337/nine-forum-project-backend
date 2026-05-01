package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.LikeThreadMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeThreadServiceImplTest {

    @Mock
    private LikeThreadMapper likeThreadMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private TipTapUtils tipTapUtils;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Test
    void shouldReturnEmptyPageWhenUserHasNoLikedThreads() {
        LikeThreadServiceImpl service = new TestableLikeThreadService(
                likeThreadMapper,
                accountMapper,
                threaddMapper,
                tipTapUtils,
                privacyPolicyService
        );
        when(accountMapper.getAccountById(2)).thenReturn(new Account(2, null, null, null, null, null, null, null, null, null, false, null));
        when(privacyPolicyService.canViewLikedThreads(null, 2)).thenReturn(true);
        when(likeThreadMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<LikeThread> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        PageEntity<ThreadVO> result = service.getLikesByAccountId(null, 2, 0, 1);

        assertNotNull(result);
        assertEquals(0L, result.getTotalSize());
        assertTrue(result.getData().isEmpty());
        verify(threaddMapper, never()).selectByIds(any());
    }

    private static final class TestableLikeThreadService extends LikeThreadServiceImpl {

        private final LikeThreadMapper likeThreadMapper;

        private TestableLikeThreadService(LikeThreadMapper likeThreadMapper,
                                          AccountMapper accountMapper,
                                          ThreaddMapper threaddMapper,
                                          TipTapUtils tipTapUtils,
                                          PrivacyPolicyService privacyPolicyService) {
            super(accountMapper, threaddMapper, tipTapUtils, privacyPolicyService);
            this.likeThreadMapper = likeThreadMapper;
        }

        @Override
        public LikeThreadMapper getBaseMapper() {
            return likeThreadMapper;
        }

        @Override
        public Class<LikeThread> getEntityClass() {
            return LikeThread.class;
        }
    }
}
