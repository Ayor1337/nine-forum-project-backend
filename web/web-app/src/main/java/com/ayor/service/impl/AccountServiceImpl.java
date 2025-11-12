package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.app.vo.UserPermissionVO;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.AccountService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService  {

    private final AccountMapper accountMapper;

    private final PermissionMapper permissionMapper;

    private final RoleMapper roleMapper;

    private final MinioService minioService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        String roleName = roleMapper.getRoleNameById(account.getRoleId());

        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(roleName)
                .build();
    }

    @Override
    @Cacheable(value = "userInfo", key = "#accountId", condition = "#accountId != null", unless = "#result == null")
    public UserInfoVO getUserInfo(Integer accountId) {
        Account account = this.getById(accountId);
        if (accountId == null) {
            return null;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(account, userInfoVO);
        UserPermissionVO userPermissionVO = permissionMapper.getUserPermissionVO(accountId);
        userInfoVO.setPermission(userPermissionVO);

        return userInfoVO;
    }

    @Override
    @CacheEvict(value = "userInfo", key = "#accountId")
    public String updateUserAvatar(Integer accountId, Base64Upload dto) {
        Account account = this.getById(accountId);
        if (account == null) {
            return "账户不存在";
        }
        try {
            String avatarUrl = minioService.uploadBase64(dto, "avatar/");
            account.setAvatarUrl(avatarUrl);
        } catch (Exception e) {
            return "资源服务器异常";
        }
        return this.baseMapper.updateById(account) > 0 ? null : "更新失败, 未知异常";
    }

    @Override
    @CacheEvict(value = "userInfo", key = "#accountId")
    public String updateUserBanner(Integer accountId, Base64Upload dto) {
        Account account = this.getById(accountId);
        if (account == null) {
            return "账户不存在";
        }
        try {
            String bannerUrl = minioService.uploadBase64(dto, "banner/");
            account.setBannerUrl(bannerUrl);
        } catch (Exception e) {
            return "资源服务器异常";
        }
        return this.baseMapper.updateById(account) > 0 ? null : "更新失败, 未知异常";
    }

    private boolean existsUserById(Integer accountId) {
        return this.baseMapper.exists(Wrappers.<Account>lambdaQuery().eq(Account::getAccountId, accountId));
    }
}
