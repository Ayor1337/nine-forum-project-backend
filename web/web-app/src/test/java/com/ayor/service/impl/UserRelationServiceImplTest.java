package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRelationServiceImplTest {

    @Mock
    private UserRelationMapper userRelationMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private UserRelationServiceImpl userRelationService;

    @Test
    void shouldRejectFollowSelf() {
        mockExistingAccount(1);

        assertEquals("不能关注自己", userRelationService.follow(1, 1));
    }

    @Test
    void shouldCreateActiveFollowWhenMissing() {
        mockExistingAccount(1);
        mockExistingAccount(2);
        when(userRelationMapper.findRelation(1, 2, RelationType.FOLLOW)).thenReturn(null);
        when(userRelationMapper.insert(any(UserRelation.class))).thenReturn(1);

        assertNull(userRelationService.follow(1, 2));
    }

    @Test
    void shouldReactivateInactiveFollow() {
        mockExistingAccount(1);
        mockExistingAccount(2);
        UserRelation relation = UserRelation.builder()
                .fromAccountId(1)
                .toAccountId(2)
                .relationType(RelationType.FOLLOW)
                .status(RelationStatus.INACTIVE)
                .build();
        when(userRelationMapper.findRelation(1, 2, RelationType.FOLLOW)).thenReturn(relation);
        when(userRelationMapper.updateById(any(UserRelation.class))).thenReturn(1);

        assertNull(userRelationService.follow(1, 2));
        assertEquals(RelationStatus.ACTIVE, relation.getStatus());
    }

    @Test
    void shouldDeactivateActiveFollowOnUnfollow() {
        mockExistingAccount(1);
        mockExistingAccount(2);
        UserRelation relation = UserRelation.builder()
                .fromAccountId(1)
                .toAccountId(2)
                .relationType(RelationType.FOLLOW)
                .status(RelationStatus.ACTIVE)
                .build();
        when(userRelationMapper.findRelation(1, 2, RelationType.FOLLOW)).thenReturn(relation);
        when(userRelationMapper.updateById(any(UserRelation.class))).thenReturn(1);

        assertNull(userRelationService.unfollow(1, 2));
        assertEquals(RelationStatus.INACTIVE, relation.getStatus());
    }

    @Test
    void shouldReportMutualFollowOnlyWhenBothDirectionsExist() {
        mockExistingAccount(1);
        mockExistingAccount(2);
        when(userRelationMapper.existsRelation(1, 2, RelationType.FOLLOW, RelationStatus.ACTIVE)).thenReturn(true);
        when(userRelationMapper.existsRelation(2, 1, RelationType.FOLLOW, RelationStatus.ACTIVE)).thenReturn(true);

        assertTrue(userRelationService.isMutualFollowing(1, 2));
    }

    @Test
    void shouldReturnFalseForBlockedCheckWhenViewerMissing() {
        assertFalse(userRelationService.isBlockedEitherDirection(0, 2));
    }

    private void mockExistingAccount(Integer accountId) {
        when(accountMapper.getAccountById(accountId)).thenReturn(new Account(accountId, null, null, null, null, null, null, null, null, null, false, null));
    }
}
