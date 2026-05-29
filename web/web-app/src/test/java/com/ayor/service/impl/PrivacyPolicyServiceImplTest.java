package com.ayor.service.impl;

import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivacyPolicyServiceImplTest {

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Test
    void shouldAllowOwnerToViewProfileAndUserProfile() {
        PrivacyPolicyServiceImpl service = createService();

        assertTrue(service.canViewProfile(12, 12));
        assertTrue(service.canViewUserProfile(12, 12));

        verify(userPrivacySettingService, never()).getByAccountId(12);
    }

    @Test
    void shouldDenyAnonymousViewerForFollowerOnlyProfile() {
        PrivacyPolicyServiceImpl service = createService();
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting(VisibilityScope.FOLLOWER_ONLY));

        assertFalse(service.canViewProfile(null, 18));
    }

    @Test
    void shouldDenyBlockedViewerEvenWhenProfileIsPublic() {
        PrivacyPolicyServiceImpl service = createService();
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting(VisibilityScope.PUBLIC));
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(true);

        assertFalse(service.canViewProfile(7, 18));
    }

    @Test
    void shouldDenyThreadRelationListsWhenBlockedEitherDirection() {
        PrivacyPolicyServiceImpl service = createService();
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting(VisibilityScope.PUBLIC));
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(true);

        assertFalse(service.canViewLikedThreads(7, 18));
        assertFalse(service.canViewCollectedThreads(7, 18));
        assertFalse(service.canViewFollowerList(7, 18));
        assertFalse(service.canViewFollowingList(7, 18));
    }

    @Test
    void shouldAllowFollowerWhenScopeIsFollowerOnly() {
        PrivacyPolicyServiceImpl service = createService();
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting(VisibilityScope.FOLLOWER_ONLY));
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(false);
        when(userRelationService.isFollowing(7, 18)).thenReturn(true);

        assertTrue(service.canViewFollowingList(7, 18));
    }

    @Test
    void shouldRequireMutualFollowForMutualFollowScope() {
        PrivacyPolicyServiceImpl service = createService();
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting(VisibilityScope.MUTUAL_FOLLOW_ONLY));
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(false);
        when(userRelationService.isMutualFollowing(7, 18)).thenReturn(false);

        assertFalse(service.canViewCollectedThreads(7, 18));
    }

    private PrivacyPolicyServiceImpl createService() {
        return new PrivacyPolicyServiceImpl(userRelationService, userPrivacySettingService);
    }

    private UserPrivacySetting setting(VisibilityScope scope) {
        return UserPrivacySetting.builder()
                .accountId(18)
                .profileVisibility(scope)
                .likedThreadsVisibility(scope)
                .collectedThreadsVisibility(scope)
                .followListVisibility(scope)
                .followerListVisibility(scope)
                .birthdayVisibility(scope)
                .build();
    }
}
