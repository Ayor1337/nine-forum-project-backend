package com.ayor.service.impl;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.mapper.AccountStatMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountStatServiceImplTest {

    @Mock
    private AccountStatMapper accountStatMapper;

    @Mock
    private LambdaQueryChainWrapper<AccountStat> queryWrapper;

    @Spy
    @InjectMocks
    private AccountStatServiceImpl accountStatService;

    @Test
    void shouldReturnStatVoWithFollowingAndFollowerCount() {
        AccountStat accountStat = new AccountStat(
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9
        );
        doReturn(queryWrapper).when(accountStatService).lambdaQuery();
        when(queryWrapper.eq(any(), any())).thenReturn(queryWrapper);
        when(queryWrapper.one()).thenReturn(accountStat);

        AccountStatVO result = accountStatService.getAccountStatByUserId(9);

        assertEquals(7, result.getFollowingCount());
        assertEquals(8, result.getFollowerCount());
        assertEquals(9, result.getAccountId());
    }

    @Test
    void shouldReturnNullWhenAccountIdIsNull() {
        assertNull(accountStatService.getAccountStatByUserId(null));
    }

    @Test
    void shouldInvokeAllAccountStatRefreshOperations() {
        accountStatService.updateAccountStat();

        verify(accountStatMapper).updateThreadCount();
        verify(accountStatMapper).updatePostCount();
        verify(accountStatMapper).updateFollowingCount();
        verify(accountStatMapper).updateFollowerCount();
    }
}
