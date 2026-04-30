package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Collect;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CollectMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PrivacyPolicyService;
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
class CollectServiceImplTest {

    @Mock
    private CollectMapper collectMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Test
    void shouldReturnEmptyPageWhenUserHasNoCollectedThreads() {
        CollectServiceImpl service = new TestableCollectService(
                collectMapper,
                accountMapper,
                threaddMapper,
                privacyPolicyService
        );
        when(accountMapper.getAccountById(2)).thenReturn(new Account(2, null, null, null, null, null, null, null, null, null, false, null));
        when(privacyPolicyService.canViewCollectedThreads(null, 2)).thenReturn(true);
        when(collectMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<Collect> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        PageEntity<ThreadVO> result = service.getCollectsByAccountId(null, 2, 0, 8);

        assertNotNull(result);
        assertEquals(0L, result.getTotalSize());
        assertTrue(result.getData().isEmpty());
        verify(threaddMapper, never()).selectByIds(any());
    }

    private static final class TestableCollectService extends CollectServiceImpl {

        private final CollectMapper collectMapper;

        private TestableCollectService(CollectMapper collectMapper,
                                       AccountMapper accountMapper,
                                       ThreaddMapper threaddMapper,
                                       PrivacyPolicyService privacyPolicyService) {
            super(accountMapper, threaddMapper, privacyPolicyService);
            this.collectMapper = collectMapper;
        }

        @Override
        public CollectMapper getBaseMapper() {
            return collectMapper;
        }

        @Override
        public Class<Collect> getEntityClass() {
            return Collect.class;
        }
    }
}
