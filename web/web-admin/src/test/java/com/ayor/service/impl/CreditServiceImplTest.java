package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.CreditAdjustDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.pojo.CreditTransaction;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CreditAccountMapper;
import com.ayor.mapper.CreditTransactionMapper;
import com.ayor.type.CreditChangeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditAccountMapper creditAccountMapper;

    @Mock
    private CreditTransactionMapper creditTransactionMapper;

    @Mock
    private AccountMapper accountMapper;

    private CreditServiceImpl creditService;

    @BeforeEach
    void setUp() {
        creditService = new CreditServiceImpl(creditTransactionMapper, accountMapper);
        ReflectionTestUtils.setField(creditService, "baseMapper", creditAccountMapper);
    }

    // 测试发放 Credit 成功且余额与流水一致
    @Test
    void shouldGrantCreditAndRecordTransaction() {
        when(accountMapper.selectById(7)).thenReturn(activeAccount(7));
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 50L));

        String result = creditService.adjustCredit(1, adjustDto(7, 100L, "活跃奖励"));

        assertThat(result).isNull();
        verify(creditAccountMapper).initAccount(7);
        verify(creditAccountMapper).updateBalance(7, 100L);
        CreditTransaction transaction = capturedTransaction();
        assertThat(transaction.getAccountId()).isEqualTo(7);
        assertThat(transaction.getDelta()).isEqualTo(100L);
        assertThat(transaction.getBalanceAfter()).isEqualTo(150L);
        assertThat(transaction.getChangeType()).isEqualTo("admin_grant");
        assertThat(transaction.getRemark()).isEqualTo("活跃奖励");
        assertThat(transaction.getOperatorId()).isEqualTo(1);
    }

    // 测试扣减 Credit 成功且余额快照连续
    @Test
    void shouldDeductCreditAndRecordTransaction() {
        when(accountMapper.selectById(7)).thenReturn(activeAccount(7));
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 150L));

        String result = creditService.adjustCredit(1, adjustDto(7, -60L, "违规扣除"));

        assertThat(result).isNull();
        verify(creditAccountMapper).updateBalance(7, -60L);
        CreditTransaction transaction = capturedTransaction();
        assertThat(transaction.getDelta()).isEqualTo(-60L);
        assertThat(transaction.getBalanceAfter()).isEqualTo(90L);
        assertThat(transaction.getChangeType()).isEqualTo("admin_deduct");
    }

    // 测试余额不足时拒绝扣减且不改变余额
    @Test
    void shouldRejectDeductWhenBalanceInsufficient() {
        when(accountMapper.selectById(7)).thenReturn(activeAccount(7));
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 50L));

        String result = creditService.adjustCredit(1, adjustDto(7, -100L, "扣减"));

        assertThat(result).contains("余额不足");
        verify(creditAccountMapper, never()).updateBalance(anyInt(), anyLong());
        verify(creditTransactionMapper, never()).insert(any(CreditTransaction.class));
    }

    // 测试调整数量为 0 时拒绝
    @Test
    void shouldRejectZeroAmount() {
        String result = creditService.adjustCredit(1, adjustDto(7, 0L, "备注"));

        assertThat(result).isEqualTo("调整数量不能为 0");
        verify(accountMapper, never()).selectById(anyInt());
    }

    // 测试调整数量超过上限时拒绝
    @Test
    void shouldRejectAmountExceedingLimit() {
        String result = creditService.adjustCredit(1, adjustDto(7, 1_000_001L, "备注"));

        assertThat(result).contains("单次调整数量不能超过");
        verify(accountMapper, never()).selectById(anyInt());
    }

    // 测试备注为空时拒绝
    @Test
    void shouldRejectBlankRemark() {
        String result = creditService.adjustCredit(1, adjustDto(7, 100L, "   "));

        assertThat(result).isEqualTo("备注不能为空");
        verify(accountMapper, never()).selectById(anyInt());
    }

    // 测试目标用户不存在或已删除时拒绝
    @Test
    void shouldRejectWhenTargetAccountMissingOrDeleted() {
        when(accountMapper.selectById(7)).thenReturn(null);
        assertThat(creditService.adjustCredit(1, adjustDto(7, 100L, "备注"))).isEqualTo("目标用户不存在");

        Account deleted = activeAccount(8);
        deleted.setDeleted(true);
        when(accountMapper.selectById(8)).thenReturn(deleted);
        assertThat(creditService.adjustCredit(1, adjustDto(8, 100L, "备注"))).isEqualTo("目标用户不存在");

        verify(creditAccountMapper, never()).initAccount(anyInt());
    }

    // 测试参数缺失时拒绝
    @Test
    void shouldRejectInvalidParams() {
        assertThat(creditService.adjustCredit(null, adjustDto(7, 100L, "备注"))).isEqualTo("参数错误");
        assertThat(creditService.adjustCredit(1, null)).isEqualTo("参数错误");
        assertThat(creditService.adjustCredit(1, adjustDto(null, 100L, "备注"))).isEqualTo("参数错误");
        assertThat(creditService.adjustCredit(1, adjustDto(7, null, "备注"))).isEqualTo("参数错误");
    }

    // 测试查询指定用户余额，无记录返回 0
    @Test
    void shouldReturnZeroBalanceWhenNoCreditRecord() {
        when(accountMapper.selectById(7)).thenReturn(activeAccount(7));
        when(creditAccountMapper.selectById(7)).thenReturn(null);

        CreditBalanceVO balance = creditService.getBalance(7);

        assertThat(balance.getAccountId()).isEqualTo(7);
        assertThat(balance.getBalance()).isEqualTo(0L);
    }

    // 测试查询不存在用户的余额返回 null
    @Test
    void shouldReturnNullBalanceWhenAccountMissing() {
        when(accountMapper.selectById(7)).thenReturn(null);

        assertThat(creditService.getBalance(7)).isNull();
    }

    // 测试流水查询筛选与排序参数透传
    @Test
    void shouldListTransactionsWithFilters() {
        CreditTransactionVO transaction = new CreditTransactionVO(
                3L, 7, "ayor", "阿尧", -30L, 70L, "admin_deduct", "扣除", 1, "管理员", new Date());
        when(creditTransactionMapper.countTransactions(7, "ayor", "admin_deduct")).thenReturn(1L);
        when(creditTransactionMapper.selectTransactions(0, 10, 7, "ayor", "admin_deduct", true))
                .thenReturn(List.of(transaction));

        PageEntity<CreditTransactionVO> page = creditService.listTransactions(
                1, 10, 7, "ayor", CreditChangeType.ADMIN_DEDUCT, "asc");

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(transaction);
    }

    // 测试流水查询空白用户名与非法分页参数归一化
    @Test
    void shouldNormalizeTransactionQueryParams() {
        when(creditTransactionMapper.countTransactions(null, null, null)).thenReturn(0L);
        when(creditTransactionMapper.selectTransactions(0, 10, null, null, null, false))
                .thenReturn(List.of());

        PageEntity<CreditTransactionVO> page = creditService.listTransactions(0, 0, null, "  ", null, "desc");

        assertThat(page.getTotalSize()).isEqualTo(0L);
        verify(creditTransactionMapper).selectTransactions(0, 10, null, null, null, false);
    }

    private CreditTransaction capturedTransaction() {
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);
        verify(creditTransactionMapper).insert(captor.capture());
        return captor.getValue();
    }

    private Account activeAccount(Integer accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setDeleted(false);
        return account;
    }

    private CreditAccount creditAccount(Integer accountId, Long balance) {
        return new CreditAccount(accountId, balance, new Date(), new Date());
    }

    private CreditAdjustDTO adjustDto(Integer accountId, Long amount, String remark) {
        CreditAdjustDTO dto = new CreditAdjustDTO();
        dto.setAccountId(accountId);
        dto.setAmount(amount);
        dto.setRemark(remark);
        return dto;
    }
}
