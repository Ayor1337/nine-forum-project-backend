package com.ayor.service.impl;

import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.VisibilityScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 隐私策略判定服务实现。
 */
@Service
@RequiredArgsConstructor
public class PrivacyPolicyServiceImpl implements PrivacyPolicyService {

    private final UserRelationService userRelationService;

    private final UserPrivacySettingService userPrivacySettingService;

    /**
     * 判断是否允许查看用户资料。
     */
    @Override
    public boolean canViewProfile(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getProfileVisibility());
    }

    /**
     * 判断是否允许查看用户点赞列表。
     */
    @Override
    public boolean canViewLikedThreads(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getLikedThreadsVisibility());
    }

    /**
     * 判断是否允许查看用户收藏列表。
     */
    @Override
    public boolean canViewCollectedThreads(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getCollectedThreadsVisibility());
    }

    /**
     * 判断是否允许查看用户粉丝列表。
     */
    @Override
    public boolean canViewFollowerList(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getFollowerListVisibility());
    }

    /**
     * 判断是否允许查看用户关注列表。
     */
    @Override
    public boolean canViewFollowingList(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getFollowListVisibility());
    }

    @Override
    public boolean canViewUserProfile(Integer viewerId, Integer ownerId) {
        if (isSelf(viewerId, ownerId)) {
            return true;
        }
        UserPrivacySetting setting = userPrivacySetting(ownerId);
        return setting != null && canAccessByScope(viewerId, ownerId, setting.getProfileVisibility());
    }

    /**
     * 按可见范围判断当前查看者是否可访问目标内容。
     */
    private boolean canAccessByScope(Integer viewerId, Integer ownerId, VisibilityScope scope) {
        if (isSelf(viewerId, ownerId)) {
            return true;
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

    /**
     * 获取目标用户的隐私设置。
     */
    private UserPrivacySetting userPrivacySetting(Integer ownerId) {
        return userPrivacySettingService.getByAccountId(ownerId);
    }

    /**
     * 判断查看者是否为资料拥有者本人。
     */
    private boolean isSelf(Integer viewerId, Integer ownerId) {
        return ownerId != null && ownerId.equals(normalizeViewerId(viewerId));
    }

    /**
     * 将空查看者ID归一化为 0，便于后续权限判断。
     */
    private Integer normalizeViewerId(Integer viewerId) {
        return viewerId == null ? 0 : viewerId;
    }
}
