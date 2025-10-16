package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService, IService<Account> {
    UserInfoVO getUserInfo(String username);

    UserInfoVO getUserInfoById(Integer id);

    String updateUserAvatar(String username, Base64Upload dto);

    String updateUserBanner(String username, Base64Upload dto);
}
