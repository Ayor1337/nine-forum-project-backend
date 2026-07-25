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
import com.ayor.service.CreditService;
import com.ayor.type.CreditChangeType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Credit 货币管理服务实现（管理端）
 *
 * 调整余额时通过行锁（SELECT ... FOR UPDATE）保证并发安全，
 * 余额更新与流水插入在同一事务内完成。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreditServiceImpl extends ServiceImpl<CreditAccountMapper, CreditAccount> implements CreditService {

    private static final long MAX_ADJUST_AMOUNT = 1_000_000L;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CreditTransactionMapper creditTransactionMapper;

    private final AccountMapper accountMapper;

    @Override
    public String adjustCredit(Integer operatorId, CreditAdjustDTO dto) {
        if (operatorId == null || dto == null || dto.getAccountId() == null || dto.getAmount() == null) {
            return "参数错误";
        }
        long amount = dto.getAmount();
        if (amount == 0) {
            return "调整数量不能为 0";
        }
        if (Math.abs(amount) > MAX_ADJUST_AMOUNT) {
            return "单次调整数量不能超过 " + MAX_ADJUST_AMOUNT;
        }
        Integer accountId = dto.getAccountId();
        Account account = accountMapper.selectById(accountId);
        if (account == null || account.isDeleted()) {
            return "目标用户不存在";
        }
        baseMapper.initAccount(accountId);
        CreditAccount creditAccount = baseMapper.selectForUpdate(accountId);
        long balanceAfter = creditAccount.getBalance() + amount;
        if (balanceAfter < 0) {
            return "余额不足，当前余额为 " + creditAccount.getBalance();
        }
        baseMapper.updateBalance(accountId, amount);
        CreditTransaction transaction = new CreditTransaction();
        transaction.setAccountId(accountId);
        transaction.setDelta(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setChangeType(CreditChangeType.fromAmount(amount).getType());
        transaction.setRemark(dto.getRemark() == null ? "" : dto.getRemark().trim());
        transaction.setOperatorId(operatorId);
        creditTransactionMapper.insert(transaction);
        return null;
    }

    @Override
    public CreditBalanceVO getBalance(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        Account account = accountMapper.selectById(accountId);
        if (account == null || account.isDeleted()) {
            return null;
        }
        CreditAccount creditAccount = this.getById(accountId);
        long balance = creditAccount == null ? 0L : creditAccount.getBalance();
        return new CreditBalanceVO(accountId, balance);
    }

    @Override
    public PageEntity<CreditTransactionVO> listTransactions(Integer pageNum,
                                                            Integer pageSize,
                                                            Integer accountId,
                                                            String username,
                                                            CreditChangeType changeType,
                                                            String sortOrder) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        String normalizedUsername = StringUtils.hasText(username) ? username : null;
        String changeTypeValue = changeType == null ? null : changeType.getType();
        boolean sortAsc = "asc".equalsIgnoreCase(sortOrder);
        return new PageEntity<>(
                creditTransactionMapper.countTransactions(accountId, normalizedUsername, changeTypeValue),
                creditTransactionMapper.selectTransactions(
                        offset,
                        normalizedPageSize,
                        accountId,
                        normalizedUsername,
                        changeTypeValue,
                        sortAsc));
    }
}
