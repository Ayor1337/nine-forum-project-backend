package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.pojo.CreditTransaction;
import com.ayor.entity.pojo.DailyCheckIn;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.mapper.CreditAccountMapper;
import com.ayor.mapper.CreditTransactionMapper;
import com.ayor.mapper.DailyCheckInMapper;
import com.ayor.type.CreditChangeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditAccountMapper creditAccountMapper;

    @Mock
    private CreditTransactionMapper creditTransactionMapper;

    @Mock
    private DailyCheckInMapper dailyCheckInMapper;

    // 测试无余额记录时返回 0
    @Test
    void shouldReturnZeroBalanceWhenNoRecord() {
        CreditServiceImpl service = createService();
        when(creditAccountMapper.selectById(7)).thenReturn(null);

        CreditBalanceVO balance = service.getBalance(7);

        assertEquals(7, balance.getAccountId());
        assertEquals(0L, balance.getBalance());
    }

    // 测试有余额记录时返回实际余额
    @Test
    void shouldReturnBalanceWhenRecordExists() {
        CreditServiceImpl service = createService();
        CreditAccount creditAccount = new CreditAccount(7, 520L, new Date(), new Date());
        when(creditAccountMapper.selectById(7)).thenReturn(creditAccount);

        CreditBalanceVO balance = service.getBalance(7);

        assertEquals(520L, balance.getBalance());
    }

    // 测试账号为空时返回 null
    @Test
    void shouldReturnNullWhenAccountIdIsNull() {
        CreditServiceImpl service = createService();

        assertNull(service.getBalance(null));
        assertNull(service.listMyTransactions(null, 1, 10));
    }

    // 测试流水分页参数归一化与结果组装
    @Test
    void shouldListMyTransactionsWithNormalizedPaging() {
        CreditServiceImpl service = createService();
        CreditTransactionVO transaction = new CreditTransactionVO(
                3L, 7, "ayor", "阿尧", 100L, 100L, "admin_grant", "奖励", 1, "管理员", new Date());
        when(creditTransactionMapper.countTransactionsByAccountId(7)).thenReturn(1L);
        when(creditTransactionMapper.selectTransactionsByAccountId(7, 0, 10)).thenReturn(List.of(transaction));

        PageEntity<CreditTransactionVO> page = service.listMyTransactions(7, 0, 0);

        assertEquals(1L, page.getTotalSize());
        assertEquals(1, page.getData().size());
        assertEquals("admin_grant", page.getData().get(0).getChangeType());
        verify(creditTransactionMapper).selectTransactionsByAccountId(7, 0, 10);
    }

    // 测试流水分页偏移量计算
    @Test
    void shouldCalculateOffsetForTransactionPage() {
        CreditServiceImpl service = createService();
        when(creditTransactionMapper.countTransactionsByAccountId(7)).thenReturn(30L);
        when(creditTransactionMapper.selectTransactionsByAccountId(7, 20, 10)).thenReturn(List.of());

        PageEntity<CreditTransactionVO> page = service.listMyTransactions(7, 3, 10);

        assertEquals(30L, page.getTotalSize());
        verify(creditTransactionMapper).selectTransactionsByAccountId(7, 20, 10);
    }

    @Test
    void shouldGrantFiveCreditsAndCreateTransactionForFirstDailyCheckIn() {
        CreditServiceImpl service = createService();
        CreditAccount creditAccount = new CreditAccount(7, 100L, new Date(), new Date());
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount);

        String message = service.checkIn(7);

        assertNull(message);
        org.mockito.ArgumentCaptor<DailyCheckIn> checkInCaptor = org.mockito.ArgumentCaptor.forClass(DailyCheckIn.class);
        verify(dailyCheckInMapper).insert(checkInCaptor.capture());
        assertEquals(7, checkInCaptor.getValue().getAccountId());
        assertEquals(LocalDate.now(ZoneId.of("Asia/Tokyo")), checkInCaptor.getValue().getCheckInDate());
        verify(creditAccountMapper).initAccount(7);
        verify(creditAccountMapper).selectForUpdate(7);
        verify(creditAccountMapper).updateBalance(7, 5L);

        org.mockito.ArgumentCaptor<CreditTransaction> transactionCaptor = org.mockito.ArgumentCaptor.forClass(CreditTransaction.class);
        verify(creditTransactionMapper).insert(transactionCaptor.capture());
        CreditTransaction transaction = transactionCaptor.getValue();
        assertEquals(5L, transaction.getDelta());
        assertEquals(105L, transaction.getBalanceAfter());
        assertEquals(CreditChangeType.DAILY_CHECK_IN.getType(), transaction.getChangeType());
        assertEquals("每日签到奖励", transaction.getRemark());
        assertEquals(7, transaction.getOperatorId());
    }

    @Test
    void shouldRejectDuplicateDailyCheckInWithoutChangingCredit() {
        CreditServiceImpl service = createService();
        when(dailyCheckInMapper.insert(any(DailyCheckIn.class)))
                .thenThrow(new DuplicateKeyException("duplicate check-in"));

        String message = service.checkIn(7);

        assertEquals("今日已签到", message);
        verifyNoInteractions(creditAccountMapper, creditTransactionMapper);
    }

    @Test
    void shouldRejectDailyCheckInWhenAccountIdIsNull() {
        CreditServiceImpl service = createService();

        assertEquals("参数错误", service.checkIn(null));
        verifyNoInteractions(dailyCheckInMapper, creditAccountMapper, creditTransactionMapper);
    }

    private CreditServiceImpl createService() {
        CreditServiceImpl service = new CreditServiceImpl(creditTransactionMapper, dailyCheckInMapper);
        ReflectionTestUtils.setField(service, "baseMapper", creditAccountMapper);
        return service;
    }
}
