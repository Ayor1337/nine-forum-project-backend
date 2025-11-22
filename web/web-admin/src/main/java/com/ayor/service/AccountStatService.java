package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountStatService extends IService<AccountStat> {

    PageEntity<AccountStat> getAccountStats(Integer pageNum, Integer pageSize, Integer accountId);

    String updateAccountStat(Integer statId, AccountStat accountStat);
}
