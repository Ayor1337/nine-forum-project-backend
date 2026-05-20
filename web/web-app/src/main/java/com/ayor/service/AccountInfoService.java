package com.ayor.service;

import com.ayor.entity.vo.AccountInfoVO;
import com.ayor.entity.pojo.AccountInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountInfoService extends IService<AccountInfo> {

    AccountInfo initDefaultIfAbsent(Integer accountId);

    AccountInfo createDefault(Integer accountId);

    AccountInfoVO getMyAccountInfo(Integer accountId);

    AccountInfoVO getPublicAccountInfo(Integer viewerId, Integer accountId);
}
