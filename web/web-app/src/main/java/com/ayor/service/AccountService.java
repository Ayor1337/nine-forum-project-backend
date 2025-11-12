package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService, IService<Account> {
    UserInfoVO getUserInfo(Integer accountId);

    String updateUserAvatar(Integer accountId, Base64Upload dto);

    String updateUserBanner(Integer accountId, Base64Upload dto);
}
