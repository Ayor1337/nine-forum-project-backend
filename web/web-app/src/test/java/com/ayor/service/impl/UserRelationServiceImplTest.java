package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void shouldDenyFollowWhenBlockedEitherDirection() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.existsBlockedEitherDirection(1, 2)).thenReturn(true);

        assertEquals("已拉黑，不能关注", service.follow(1, 2));

        verify(userRelationMapper, never()).findRelation(1, 2, RelationType.FOLLOW);
        verify(userRelationMapper, never()).insert(any(UserRelation.class));
    }

    @Test
    void shouldCheckDirectionalBlockedRelation() {
        UserRelationServiceImpl service = createService();
        mockValidPair(1, 2);
        when(userRelationMapper.existsRelation(1, 2, RelationType.BLOCK, RelationStatus.ACTIVE)).thenReturn(true);

        assertEquals(true, service.isBlocked(1, 2));
    }

    private UserRelationServiceImpl createService() {
        return new UserRelationServiceImpl(userRelationMapper, accountMapper, userProfileMapper);
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
