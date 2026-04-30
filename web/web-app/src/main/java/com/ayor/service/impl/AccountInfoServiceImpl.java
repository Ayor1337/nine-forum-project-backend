package com.ayor.service.impl;

import com.ayor.entity.app.vo.AccountInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.AccountInfo;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.AccountInfoMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.AccountInfoService;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.VisibilityScope;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountInfoServiceImpl extends ServiceImpl<AccountInfoMapper, AccountInfo> implements AccountInfoService {

    private final AccountInfoMapper accountInfoMapper;

    private final AccountMapper accountMapper;

    private final PrivacyPolicyService privacyPolicyService;

    private final UserPrivacySettingService userPrivacySettingService;

    private final UserRelationService userRelationService;

    @Override
    public AccountInfo initDefaultIfAbsent(Integer accountId) {
        if (accountId == null || accountId <= 0) {
            return null;
        }
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        AccountInfo accountInfo = accountInfoMapper.selectById(accountId);
        return accountInfo == null ? createDefault(accountId) : accountInfo;
    }

    @Override
    public AccountInfo createDefault(Integer accountId) {
        if (accountId == null || accountId <= 0 || accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        AccountInfo accountInfo = AccountInfo.builder()
                .accountId(accountId)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        accountInfoMapper.insert(accountInfo);
        return accountInfo;
    }

    @Override
    public AccountInfoVO getMyAccountInfo(Integer accountId) {
        return toVO(initDefaultIfAbsent(accountId));
    }

    @Override
    public AccountInfoVO getPublicAccountInfo(Integer viewerId, Integer accountId) {
        if (accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        if (!privacyPolicyService.canViewProfile(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看该用户资料");
        }
        AccountInfoVO vo = toVO(initDefaultIfAbsent(accountId));
        if (vo == null) {
            return null;
        }
        UserPrivacySetting setting = userPrivacySettingService.getByAccountId(accountId);
        if (setting == null || !canAccessByScope(viewerId, accountId, setting.getBirthdayVisibility())) {
            vo.setBirthday(null);
        }
        return vo;
    }

    private AccountInfoVO toVO(AccountInfo accountInfo) {
        if (accountInfo == null) {
            return null;
        }
        AccountInfoVO vo = new AccountInfoVO();
        BeanUtils.copyProperties(accountInfo, vo);
        return vo;
    }

    private boolean canAccessByScope(Integer viewerId, Integer ownerId, VisibilityScope scope) {
        if (ownerId != null && ownerId.equals(normalizeViewerId(viewerId))) {
            return true;
        }
        if (scope == null) {
            return false;
        }
        if (userRelationService.isBlockedEitherDirection(normalizeViewerId(viewerId), ownerId)) {
            return false;
        }
        if (scope == VisibilityScope.PUBLIC) {
            return true;
        }
        if (normalizeViewerId(viewerId) <= 0) {
            return false;
        }
        if (scope == VisibilityScope.PRIVATE) {
            return false;
        }
        if (scope == VisibilityScope.FOLLOWER_ONLY) {
            return userRelationService.isFollowing(viewerId, ownerId);
        }
        return userRelationService.isMutualFollowing(viewerId, ownerId);
    }

    private Integer normalizeViewerId(Integer viewerId) {
        return viewerId == null ? 0 : viewerId;
    }
}
