package com.ayor.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.dto.AccountProfileDTO;
import com.ayor.entity.dto.PasswordChangeDTO;
import com.ayor.entity.vo.AccountInfoVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.UserPermissionVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.AccountInfo;
import com.ayor.mapper.AccountInfoMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.AccountInfoService;
import com.ayor.service.AccountService;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.util.JWTUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService  {

    private final AccountMapper accountMapper;

    private final AccountInfoMapper accountInfoMapper;

    private final PermissionMapper permissionMapper;

    private final RoleMapper roleMapper;

    private final MinioService minioService;

    private final PasswordEncoder passwordEncoder;

    private final AccountStatMapper accountStatMapper;

    private final JWTUtils jwtUtils;

    private final PasswordEncoder encoder;

    private final UserRelationService userRelationService;

    private final PrivacyPolicyService privacyPolicyService;

    private final UserPrivacySettingService userPrivacySettingService;

    private final AccountInfoService accountInfoService;

    /**
     * 根据用户名加载 Spring Security 登录信息。
     */

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
    /**
     * 获取用户资料和权限信息。
     */

    @Override
    @Cacheable(value = "userInfo", key = "#accountId", condition = "#accountId != null", unless = "#result == null")
    public UserInfoVO getUserInfo(Integer accountId) {
        Account account = this.getById(accountId);
        if (accountId == null) {
            return null;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(account, userInfoVO);
        fillBio(userInfoVO, accountId);
        UserPermissionVO userPermissionVO = permissionMapper.getUserPermissionVO(accountId);
        userInfoVO.setPermission(userPermissionVO);

        return userInfoVO;
    }

    /**
     * 获取指定用户的公开资料并检查查看权限。
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 目标用户ID
     * @return 公开用户资料
     */
    @Override
    public UserInfoVO getPublicUserInfo(Integer viewerId, Integer accountId) {
        Account account = this.getById(accountId);
        if (account == null) {
            return null;
        }
        if (!privacyPolicyService.canViewProfile(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看该用户资料");
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(account, userInfoVO);
        fillBio(userInfoVO, accountId);
        userInfoVO.setPermission(null);
        return userInfoVO;
    }

    @Override
    public AccountInfoVO getMyAccountInfo(Integer accountId) {
        return accountInfoService.getMyAccountInfo(accountId);
    }

    @Override
    public AccountInfoVO getPublicAccountInfo(Integer viewerId, Integer accountId) {
        return accountInfoService.getPublicAccountInfo(viewerId, accountId);
    }

    /**
     * 获取指定用户的粉丝列表并应用隐私校验。
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 目标用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户粉丝列表
     */
    @Override
    public PageEntity<UserInfoVO> getFollowers(Integer viewerId, Integer accountId, Integer pageNum, Integer pageSize) {
        if (this.getById(accountId) == null) {
            return null;
        }
        if (!privacyPolicyService.canViewFollowerList(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看粉丝列表");
        }
        return userRelationService.getFollowers(accountId, pageNum, pageSize);
    }

    /**
     * 获取指定用户的关注列表并应用隐私校验。
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 目标用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户关注列表
     */
    @Override
    public PageEntity<UserInfoVO> getFollowings(Integer viewerId, Integer accountId, Integer pageNum, Integer pageSize) {
        if (this.getById(accountId) == null) {
            return null;
        }
        if (!privacyPolicyService.canViewFollowingList(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看关注列表");
        }
        return userRelationService.getFollowings(accountId, pageNum, pageSize);
    }
    /**
     * 更新用户头像并同步到对象存储。
     */

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
    /**
     * 更新用户横幅图并同步到对象存储。
     */

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
    /**
     * 校验邮箱验证 token 后创建新账户。
     */

    @Override
    public String insertNewAccount(AccountDTO accountDTO) {
        if (existsUserByUsername(accountDTO.getUsername())) {
            return "用户名已存在";
        }
        DecodedJWT decodedJWT = jwtUtils.resolveEmailJwt(accountDTO.getToken());
        if (decodedJWT == null) {
            return "验证失败";
        }
        Account account = new Account();
        BeanUtils.copyProperties(accountDTO, account);
        account.setAvatarUrl("nineforum/avatar/default.jpg");
        account.setBannerUrl("nineforum/banner/default.webp");
        account.setEmail(decodedJWT.getClaim("email").asString());
        account.setCreateTime(new Date());
        String encodePwd = passwordEncoder.encode(account.getPassword());
        account.setStatus(1);
        account.setRoleId(3);
        account.setPassword(encodePwd);
        if (this.save(account)) {
            accountInfoService.initDefaultIfAbsent(account.getAccountId());
            userPrivacySettingService.initDefaultIfAbsent(account.getAccountId());
            return accountStatMapper.insertNewAccountStat(account.getAccountId()) ? null : "添加统计数据失败";
        }
        return "添加失败, 未知异常";
    }

    /**
     * 更新用户个人资料
     */
    @Override
    @CacheEvict(value = "userInfo", key = "#accountId")
    public String updateUserProfile(Integer accountId, AccountProfileDTO profileDTO) {
        if (Objects.isNull(profileDTO)) {
            return "上传的用户信息为空";
        }

        Account accountById = this.accountMapper.getAccountById(accountId);

        if (profileDTO.getAvatar() != null) {
            this.updateUserAvatar(accountId, profileDTO.getAvatar());
        }

        if (accountById == null) {
            return "用户不存在";
        }
        if (!isValidWebsite(profileDTO.getWebsite())) {
            return "个人网站格式有误";
        }

        if (profileDTO.getNickname() != null) {
            accountById.setNickname(profileDTO.getNickname());
        }

        AccountInfo accountInfo = accountInfoService.initDefaultIfAbsent(accountId);
        if (accountInfo == null) {
            return "用户不存在";
        }
        accountInfo.setBio(profileDTO.getBio());
        accountInfo.setLocation(profileDTO.getLocation());
        accountInfo.setBirthday(profileDTO.getBirthday());
        accountInfo.setWebsite(profileDTO.getWebsite());
        accountInfo.setUpdateTime(new Date());

        if (!this.updateById(accountById)) {
            return "修改失败";
        }
        return accountInfoMapper.updateById(accountInfo) > 0 ? null : "修改失败";
    }

    /**
     * 更新用户密码
     * */
    @Override
    public String updatePasswordWithOld(String token, PasswordChangeDTO pwDto) {
        Integer accountId = jwtUtils.toId(jwtUtils.resolveJwt(token));

        if (Objects.isNull(pwDto)) {
            return "密码不可为空";
        }
        Account account = this.accountMapper.getAccountById(accountId);

        if (Objects.isNull(account)) {
            return "当前用户不存在";
        }

        if (!encoder.matches(pwDto.getOldPassword(), account.getPassword())) {
            return "当前密码有误";
        }

        if (encoder.matches(pwDto.getNewPassword(), account.getPassword())) {
            return "新的密码不能和旧的密码相同";
        }

        account.setPassword(encoder.encode(pwDto.getNewPassword()));

        if (this.updateById(account)) {
            jwtUtils.invalidateJWT(token);
            return null;
        } else {
            return "更新密码失败";
        }

    }

    /**
     * 判断指定用户 ID 是否存在。
     */
    private boolean existsUserById(Integer accountId) {
        return this.baseMapper.exists(Wrappers.<Account>lambdaQuery().eq(Account::getAccountId, accountId));
    }

    /**
     * 判断指定用户名是否已存在。
     */
    private boolean existsUserByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>lambdaQuery().eq(Account::getUsername, username));
    }

    private void fillBio(UserInfoVO userInfoVO, Integer accountId) {
        AccountInfo accountInfo = accountInfoMapper.selectById(accountId);
        userInfoVO.setBio(accountInfo == null ? null : accountInfo.getBio());
    }

    private boolean isValidWebsite(String website) {
        if (website == null || website.isBlank()) {
            return true;
        }
        try {
            URI uri = new URI(website);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
