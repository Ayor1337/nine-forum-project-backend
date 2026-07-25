package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.mapper.CreditAccountMapper;
import com.ayor.mapper.CreditTransactionMapper;
import com.ayor.service.CreditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Credit 货币查询服务实现（用户端）
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreditServiceImpl extends ServiceImpl<CreditAccountMapper, CreditAccount> implements CreditService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CreditTransactionMapper creditTransactionMapper;

    @Override
    public CreditBalanceVO getBalance(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        CreditAccount creditAccount = this.getById(accountId);
        long balance = creditAccount == null ? 0L : creditAccount.getBalance();
        return new CreditBalanceVO(accountId, balance);
    }

    @Override
    public PageEntity<CreditTransactionVO> listMyTransactions(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return null;
        }
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        return new PageEntity<>(
                creditTransactionMapper.countTransactionsByAccountId(accountId),
                creditTransactionMapper.selectTransactionsByAccountId(accountId, offset, normalizedPageSize));
    }
}
