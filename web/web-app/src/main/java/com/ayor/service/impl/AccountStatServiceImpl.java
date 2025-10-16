package com.ayor.service.impl;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.service.AccountStatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AccountStatServiceImpl extends ServiceImpl<AccountStatMapper, AccountStat> implements AccountStatService {

    @Resource
    private AccountStatMapper accountStatMapper;

    @Resource
    private AccountMapper accountMapper;


    @Override
    public AccountStatVO getAccountStatByUsername(String username) {
        Integer userId = accountMapper.getAccountIdByUsername(username);
        if (userId == null) {
            return null;
        }
        AccountStat accountStat = this.lambdaQuery().eq(AccountStat::getAccountId, userId).one();
        if (accountStat == null) {
            return null;
        }
        AccountStatVO accountStatVO = new AccountStatVO();
        BeanUtils.copyProperties(accountStat, accountStatVO);

        return accountStatVO;
    }


    @Override
    public void updateAccountStat() {
        accountStatMapper.updateThreadCount();
        accountStatMapper.updatePostCount();
    }

}
