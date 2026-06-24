package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.ayor.service.CacheInvalidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRelationServiceImplTest {

    @Mock
    private UserRelationMapper userRelationMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    // 测试创建拉黑关系后停用双方有效关注关系
    @Test
    void shouldDeactivateBothActiveFollowsAfterBlockCreated() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.findRelation(1, 2, RelationType.BLOCK)).thenReturn(null);
        when(userRelationMapper.insert(any(UserRelation.class))).thenReturn(1);
        UserRelation forwardFollow = relation(1, 2, RelationType.FOLLOW, RelationStatus.ACTIVE);
        UserRelation backwardFollow = relation(2, 1, RelationType.FOLLOW, RelationStatus.ACTIVE);
        when(userRelationMapper.findRelation(1, 2, RelationType.FOLLOW)).thenReturn(forwardFollow);
        when(userRelationMapper.findRelation(2, 1, RelationType.FOLLOW)).thenReturn(backwardFollow);
        when(userRelationMapper.updateById(any(UserRelation.class))).thenReturn(1);

        assertNull(service.block(1, 2));

        assertEquals(RelationStatus.INACTIVE, forwardFollow.getStatus());
        assertEquals(RelationStatus.INACTIVE, backwardFollow.getStatus());
        verify(userRelationMapper).updateById(forwardFollow);
        verify(userRelationMapper).updateById(backwardFollow);
    }

    // 测试已拉黑时仍停用残留关注关系
    @Test
    void shouldDeactivateResidualFollowsWhenAlreadyBlocked() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.findRelation(1, 2, RelationType.BLOCK))
                .thenReturn(relation(1, 2, RelationType.BLOCK, RelationStatus.ACTIVE));
        UserRelation forwardFollow = relation(1, 2, RelationType.FOLLOW, RelationStatus.ACTIVE);
        when(userRelationMapper.findRelation(1, 2, RelationType.FOLLOW)).thenReturn(forwardFollow);
        when(userRelationMapper.findRelation(2, 1, RelationType.FOLLOW)).thenReturn(null);
        when(userRelationMapper.updateById(any(UserRelation.class))).thenReturn(1);

        assertEquals("已拉黑", service.block(1, 2));

        assertEquals(RelationStatus.INACTIVE, forwardFollow.getStatus());
        verify(userRelationMapper).updateById(forwardFollow);
    }

    // 测试拒绝关注当拉黑任一方向
    @Test
    void shouldDenyFollowWhenBlockedEitherDirection() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.existsBlockedEitherDirection(1, 2)).thenReturn(true);

        assertEquals("已拉黑，不能关注", service.follow(1, 2));

        verify(userRelationMapper, never()).findRelation(1, 2, RelationType.FOLLOW);
        verify(userRelationMapper, never()).insert(any(UserRelation.class));
    }

    // 测试检查单向拉黑关系
    @Test
    void shouldCheckDirectionalBlockedRelation() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.existsRelation(1, 2, RelationType.BLOCK, RelationStatus.ACTIVE)).thenReturn(true);

        assertEquals(true, service.isBlocked(1, 2));
    }

    // 测试列出任一方向的拉黑账号ID
    @Test
    void shouldListBlockedAccountIdsEitherDirection() {
        UserRelationServiceImpl service = createService();
        when(userRelationMapper.listBlockedAccountIdsEitherDirection(7)).thenReturn(List.of(11, 12));

        assertEquals(List.of(11, 12), service.listBlockedAccountIdsEitherDirection(7));
    }

    // 测试账号ID为空时返回空拉黑账号ID列表
    @Test
    void shouldReturnEmptyBlockedAccountIdsWhenAccountIdIsNull() {
        UserRelationServiceImpl service = createService();

        assertEquals(List.of(), service.listBlockedAccountIdsEitherDirection(null));

        verify(userRelationMapper, never()).listBlockedAccountIdsEitherDirection(any());
    }

    private UserRelationServiceImpl createService() {
        return new UserRelationServiceImpl(userRelationMapper, accountMapper, userProfileMapper, cacheInvalidationService);
    }

    private void mockValidPair(Integer firstAccountId, Integer secondAccountId) {
        when(accountMapper.getAccountById(firstAccountId)).thenReturn(account(firstAccountId));
        when(accountMapper.getAccountById(secondAccountId)).thenReturn(account(secondAccountId));
    }

    private Account account(Integer accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        return account;
    }

    private UserRelation relation(Integer fromAccountId, Integer toAccountId, RelationType type, RelationStatus status) {
        return UserRelation.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .relationType(type)
                .status(status)
                .build();
    }
}
