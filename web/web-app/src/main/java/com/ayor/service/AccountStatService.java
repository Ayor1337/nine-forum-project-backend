package com.ayor.service;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.entity.pojo.AccountStat;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountStatService extends IService<AccountStat> {

    AccountStatVO getAccountStatByUsername(String username);

    void updateAccountStat();
}
