package com.ayor.service.impl;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.service.AccountStatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountStatServiceImpl extends ServiceImpl<AccountStatMapper, AccountStat> implements AccountStatService {

    private final AccountStatMapper accountStatMapper;
    /**
     * 根据用户 ID 获取账号统计信息。
     */

    @Override
    public AccountStatVO getAccountStatByUsername(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        AccountStat accountStat = this.lambdaQuery().eq(AccountStat::getAccountId, accountId).one();
        if (accountStat == null) {
            return null;
        }
        AccountStatVO accountStatVO = new AccountStatVO();
        BeanUtils.copyProperties(accountStat, accountStatVO);

        return accountStatVO;
    }
    /**
     * 刷新所有用户的统计数据。
     */


    @Override
    public void updateAccountStat() {
        accountStatMapper.updateThreadCount();
        accountStatMapper.updatePostCount();
    }

}
