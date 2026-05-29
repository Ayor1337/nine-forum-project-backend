package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserProfile;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.service.UserRelationService;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户关系服务实现。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserRelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation> implements UserRelationService {

    private final UserRelationMapper userRelationMapper;

    private final AccountMapper accountMapper;

    private final UserProfileMapper userProfileMapper;

    /**
     * 关注指定用户。
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userRelationFollowing", key = "#fromAccountId + ':' + #toAccountId", condition = "#fromAccountId != null && #toAccountId != null"),
            @CacheEvict(value = "userRelationMutualFollowing", key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#fromAccountId, #toAccountId)", condition = "#fromAccountId != null && #toAccountId != null")
    })
    public String follow(Integer fromAccountId, Integer toAccountId) {
        return upsertRelation(fromAccountId, toAccountId, RelationType.FOLLOW, "不能关注自己", "已关注");
    }

    /**
     * 取消关注指定用户。
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userRelationFollowing", key = "#fromAccountId + ':' + #toAccountId", condition = "#fromAccountId != null && #toAccountId != null"),
            @CacheEvict(value = "userRelationMutualFollowing", key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#fromAccountId, #toAccountId)", condition = "#fromAccountId != null && #toAccountId != null")
    })
    public String unfollow(Integer fromAccountId, Integer toAccountId) {
        return deactivateRelation(fromAccountId, toAccountId, RelationType.FOLLOW, "尚未关注");
    }

    /**
     * 拉黑指定用户。
     */
    @Override
    @CacheEvict(value = "userRelationBlocked", key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#fromAccountId, #toAccountId)", condition = "#fromAccountId != null && #toAccountId != null")
    public String block(Integer fromAccountId, Integer toAccountId) {
        return upsertRelation(fromAccountId, toAccountId, RelationType.BLOCK, "不能拉黑自己", "已拉黑");
    }

    /**
     * 取消拉黑指定用户。
     */
    @Override
    @CacheEvict(value = "userRelationBlocked", key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#fromAccountId, #toAccountId)", condition = "#fromAccountId != null && #toAccountId != null")
    public String unblock(Integer fromAccountId, Integer toAccountId) {
        return deactivateRelation(fromAccountId, toAccountId, RelationType.BLOCK, "尚未拉黑");
    }

    /**
     * 判断两个用户之间是否存在关注关系。
     */
    @Override
    @Cacheable(value = "userRelationFollowing", key = "#fromAccountId + ':' + #toAccountId",
            condition = "#fromAccountId != null && #toAccountId != null")
    public boolean isFollowing(Integer fromAccountId, Integer toAccountId) {
        if (!isValidUserPair(fromAccountId, toAccountId)) {
            return false;
        }
        return userRelationMapper.existsRelation(fromAccountId, toAccountId, RelationType.FOLLOW, RelationStatus.ACTIVE);
    }

    /**
     * 判断两个用户是否互相关注。
     */
    @Override
    @Cacheable(value = "userRelationMutualFollowing",
            key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#firstAccountId, #secondAccountId)",
            condition = "#firstAccountId != null && #secondAccountId != null")
    public boolean isMutualFollowing(Integer firstAccountId, Integer secondAccountId) {
        return isFollowing(firstAccountId, secondAccountId) && isFollowing(secondAccountId, firstAccountId);
    }

    /**
     * 判断两个用户之间是否存在任一方向的拉黑关系。
     */
    @Override
    @Cacheable(value = "userRelationBlocked",
            key = "T(com.ayor.service.impl.UserRelationServiceImpl).symmetricKey(#firstAccountId, #secondAccountId)",
            condition = "#firstAccountId != null && #secondAccountId != null")
    public boolean isBlockedEitherDirection(Integer firstAccountId, Integer secondAccountId) {
        if (!isValidUserPair(firstAccountId, secondAccountId)) {
            return false;
        }
        return userRelationMapper.existsBlockedEitherDirection(firstAccountId, secondAccountId);
    }

    /**
     * 获取用户粉丝列表。
     */
    @Override
    public PageEntity<UserInfoVO> getFollowers(Integer accountId, Integer pageNum, Integer pageSize) {
        Page<UserRelation> page = this.lambdaQuery()
                .eq(UserRelation::getToAccountId, accountId)
                .eq(UserRelation::getRelationType, RelationType.FOLLOW)
                .eq(UserRelation::getStatus, RelationStatus.ACTIVE)
                .orderByDesc(UserRelation::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
        List<Integer> accountIds = page.getRecords().stream().map(UserRelation::getFromAccountId).toList();
        return new PageEntity<>(page.getTotal(), toUserInfos(accountIds));
    }

    /**
     * 获取用户关注列表。
     */
    @Override
    public PageEntity<UserInfoVO> getFollowings(Integer accountId, Integer pageNum, Integer pageSize) {
        Page<UserRelation> page = this.lambdaQuery()
                .eq(UserRelation::getFromAccountId, accountId)
                .eq(UserRelation::getRelationType, RelationType.FOLLOW)
                .eq(UserRelation::getStatus, RelationStatus.ACTIVE)
                .orderByDesc(UserRelation::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
        List<Integer> accountIds = page.getRecords().stream().map(UserRelation::getToAccountId).toList();
        return new PageEntity<>(page.getTotal(), toUserInfos(accountIds));
    }

    /**
     * 创建或恢复一条用户关系记录。
     */
    private String upsertRelation(Integer fromAccountId, Integer toAccountId, RelationType relationType,
                                  String selfMessage, String duplicateMessage) {
        if (!isValidUserPair(fromAccountId, toAccountId)) {
            return "用户不存在";
        }
        if (fromAccountId.equals(toAccountId)) {
            return selfMessage;
        }
        UserRelation relation = userRelationMapper.findRelation(fromAccountId, toAccountId, relationType);
        if (relation == null) {
            UserRelation newRelation = UserRelation.builder()
                    .fromAccountId(fromAccountId)
                    .toAccountId(toAccountId)
                    .relationType(relationType)
                    .status(RelationStatus.ACTIVE)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            return userRelationMapper.insert(newRelation) > 0 ? null : "操作失败";
        }
        if (relation.getStatus() == RelationStatus.ACTIVE) {
            return duplicateMessage;
        }
        relation.setStatus(RelationStatus.ACTIVE);
        relation.setUpdateTime(new Date());
        return userRelationMapper.updateById(relation) > 0 ? null : "操作失败";
    }

    /**
     * 将指定用户关系置为失效。
     */
    private String deactivateRelation(Integer fromAccountId, Integer toAccountId, RelationType relationType, String notFoundMessage) {
        if (!isValidUserPair(fromAccountId, toAccountId)) {
            return "用户不存在";
        }
        UserRelation relation = userRelationMapper.findRelation(fromAccountId, toAccountId, relationType);
        if (relation == null || relation.getStatus() == RelationStatus.INACTIVE) {
            return notFoundMessage;
        }
        relation.setStatus(RelationStatus.INACTIVE);
        relation.setUpdateTime(new Date());
        return userRelationMapper.updateById(relation) > 0 ? null : "操作失败";
    }

    /**
     * 判断两个账号是否都存在。
     */
    private boolean isValidUserPair(Integer firstAccountId, Integer secondAccountId) {
        return firstAccountId != null
                && secondAccountId != null
                && accountMapper.getAccountById(firstAccountId) != null
                && accountMapper.getAccountById(secondAccountId) != null;
    }

    /**
     * 将账号ID列表转换为用户信息列表。
     */
    private List<UserInfoVO> toUserInfos(List<Integer> accountIds) {
        if (accountIds.isEmpty()) {
            return List.of();
        }
        List<Account> accounts = this.listUserAccounts(accountIds);
        List<UserInfoVO> userInfoVOS = new ArrayList<>();
        for (Integer accountId : accountIds) {
            accounts.stream()
                    .filter(account -> accountId.equals(account.getAccountId()))
                    .findFirst()
                    .ifPresent(account -> userInfoVOS.add(toUserInfo(account)));
        }
        return userInfoVOS;
    }

    /**
     * 批量查询用户实体。
     */
    private List<Account> listUserAccounts(List<Integer> accountIds) {
        return accountIds.isEmpty() ? List.of() : accountMapper.getAccountsByIds(accountIds);
    }

    /**
     * 将用户实体转换为用户信息视图对象。
     */
    private UserInfoVO toUserInfo(Account account) {
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(account, userInfoVO);
        UserProfile userProfile = userProfileMapper.selectById(account.getAccountId());
        userInfoVO.setBio(userProfile == null ? null : userProfile.getBio());
        userInfoVO.setPermission(null);
        return userInfoVO;
    }

    public static String symmetricKey(Integer firstAccountId, Integer secondAccountId) {
        if (firstAccountId == null || secondAccountId == null) {
            return "0:0";
        }
        int left = Math.min(firstAccountId, secondAccountId);
        int right = Math.max(firstAccountId, secondAccountId);
        return left + ":" + right;
    }

}
