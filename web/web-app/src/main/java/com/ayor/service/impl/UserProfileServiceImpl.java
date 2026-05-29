package com.ayor.service.impl;

import com.ayor.entity.vo.UserProfileVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserProfile;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.UserProfileService;
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
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    private final UserProfileMapper userProfileMapper;

    private final AccountMapper accountMapper;

    private final PrivacyPolicyService privacyPolicyService;

    private final UserPrivacySettingService userPrivacySettingService;

    private final UserRelationService userRelationService;

    @Override
    public UserProfile initDefaultIfAbsent(Integer accountId) {
        if (accountId == null || accountId <= 0) {
            return null;
        }
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        UserProfile userProfile = userProfileMapper.selectById(accountId);
        return userProfile == null ? createDefault(accountId) : userProfile;
    }

    @Override
    public UserProfile createDefault(Integer accountId) {
        if (accountId == null || accountId <= 0 || accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        UserProfile userProfile = UserProfile.builder()
                .accountId(accountId)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        userProfileMapper.insert(userProfile);
        return userProfile;
    }

    @Override
    public UserProfileVO getMyProfile(Integer accountId) {
        return toVO(initDefaultIfAbsent(accountId));
    }

    @Override
    public UserProfileVO getPublicProfile(Integer viewerId, Integer accountId) {
        if (accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        if (!privacyPolicyService.canViewUserProfile(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看该用户资料");
        }
        UserProfileVO vo = toVO(initDefaultIfAbsent(accountId));
        if (vo == null) {
            return null;
        }
        UserPrivacySetting setting = userPrivacySettingService.getByAccountId(accountId);
        if (setting == null || !canAccessByScope(viewerId, accountId, setting.getBirthdayVisibility())) {
            vo.setBirthday(null);
        }
        return vo;
    }

    private UserProfileVO toVO(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(userProfile, vo);
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
