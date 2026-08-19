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
import com.ayor.service.CreditService;
import com.ayor.type.CreditChangeType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Credit 货币查询服务实现（用户端）
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreditServiceImpl extends ServiceImpl<CreditAccountMapper, CreditAccount> implements CreditService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final long DAILY_CHECK_IN_REWARD = 5L;

    private static final ZoneId CHECK_IN_ZONE_ID = ZoneId.of("Asia/Tokyo");

    private final CreditTransactionMapper creditTransactionMapper;

    private final DailyCheckInMapper dailyCheckInMapper;

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
    public String checkIn(Integer accountId) {
        if (accountId == null) {
            return "参数错误";
        }

        DailyCheckIn dailyCheckIn = new DailyCheckIn();
        dailyCheckIn.setAccountId(accountId);
        dailyCheckIn.setCheckInDate(LocalDate.now(CHECK_IN_ZONE_ID));
        try {
            dailyCheckInMapper.insert(dailyCheckIn);
        } catch (DuplicateKeyException exception) {
            return "今日已签到";
        }

        baseMapper.initAccount(accountId);
        CreditAccount creditAccount = baseMapper.selectForUpdate(accountId);
        baseMapper.updateBalance(accountId, DAILY_CHECK_IN_REWARD);

        CreditTransaction transaction = new CreditTransaction();
        transaction.setAccountId(accountId);
        transaction.setDelta(DAILY_CHECK_IN_REWARD);
        transaction.setBalanceAfter(creditAccount.getBalance() + DAILY_CHECK_IN_REWARD);
        transaction.setChangeType(CreditChangeType.DAILY_CHECK_IN.getType());
        transaction.setRemark("每日签到奖励");
        transaction.setOperatorId(accountId);
        creditTransactionMapper.insert(transaction);
        return null;
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
