package com.ayor.service.impl;

import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivacyPolicyServiceImplTest {

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @InjectMocks
    private PrivacyPolicyServiceImpl privacyPolicyService;

    @Test
    void shouldAllowSelfProfileAccess() {
        assertTrue(privacyPolicyService.canViewProfile(1, 1));
    }

    @Test
    void shouldDenyAnyAccessWhenBlocked() {
        when(userRelationService.isBlockedEitherDirection(2, 1)).thenReturn(true);

        assertFalse(privacyPolicyService.canViewProfile(2, 1));
        assertFalse(privacyPolicyService.canStartConversation(2, 1));
    }

    @Test
    void shouldAllowFollowerOnlyProfileForFollowers() {
        UserPrivacySetting setting = privacySetting(VisibilityScope.FOLLOWER_ONLY, DmPermission.EVERYONE);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(setting);
        when(userRelationService.isBlockedEitherDirection(2, 1)).thenReturn(false);
        when(userRelationService.isFollowing(2, 1)).thenReturn(true);

        assertTrue(privacyPolicyService.canViewProfile(2, 1));
    }

    @Test
    void shouldRequireMutualFollowForCollections() {
        UserPrivacySetting setting = privacySetting(VisibilityScope.MUTUAL_FOLLOW_ONLY, DmPermission.EVERYONE);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(setting);
        when(userRelationService.isBlockedEitherDirection(2, 1)).thenReturn(false);
        when(userRelationService.isMutualFollowing(2, 1)).thenReturn(false);

        assertFalse(privacyPolicyService.canViewCollectedThreads(2, 1));
    }

    @Test
    void shouldDenyPrivateLikedThreadsForOthers() {
        UserPrivacySetting setting = privacySetting(VisibilityScope.PRIVATE, DmPermission.EVERYONE);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(setting);
        when(userRelationService.isBlockedEitherDirection(2, 1)).thenReturn(false);

        assertFalse(privacyPolicyService.canViewLikedThreads(2, 1));
    }

    @Test
    void shouldDenyConversationWhenNobodyAllowed() {
        UserPrivacySetting setting = privacySetting(VisibilityScope.PUBLIC, DmPermission.NOBODY);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(setting);
        when(userRelationService.isBlockedEitherDirection(2, 1)).thenReturn(false);

        assertFalse(privacyPolicyService.canStartConversation(2, 1));
    }

    private UserPrivacySetting privacySetting(VisibilityScope visibilityScope, DmPermission dmPermission) {
        return UserPrivacySetting.builder()
                .accountId(1)
                .profileVisibility(visibilityScope)
                .likedThreadsVisibility(visibilityScope)
                .collectedThreadsVisibility(visibilityScope)
                .followListVisibility(visibilityScope)
                .followerListVisibility(visibilityScope)
                .dmPermission(dmPermission)
                .build();
    }
}
