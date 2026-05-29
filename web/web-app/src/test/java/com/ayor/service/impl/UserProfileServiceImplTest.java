package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserProfile;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.entity.vo.UserProfileVO;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Mock
    private UserRelationService userRelationService;

    @Test
    void getMyProfileShouldReturnProfileOnly() {
        UserProfileServiceImpl service = createService();
        when(accountMapper.getAccountById(7)).thenReturn(account(7));
        when(userProfileMapper.selectById(7)).thenReturn(userProfile(7));

        UserProfileVO result = service.getMyProfile(7);

        assertNotNull(result);
        verify(userRelationService, never()).isBlocked(7, 7);
    }

    @Test
    void getPublicProfileShouldReturnProfileOnly() {
        UserProfileServiceImpl service = createService();
        when(accountMapper.getAccountById(18)).thenReturn(account(18));
        when(privacyPolicyService.canViewUserProfile(7, 18)).thenReturn(true);
        when(userProfileMapper.selectById(18)).thenReturn(userProfile(18));
        when(userPrivacySettingService.getByAccountId(18)).thenReturn(setting());

        UserProfileVO result = service.getPublicProfile(7, 18);

        assertNotNull(result);
        verify(userRelationService, never()).isBlocked(7, 18);
        verify(userRelationService, never()).isBlocked(18, 7);
    }

    private UserProfileServiceImpl createService() {
        UserProfileServiceImpl service = new UserProfileServiceImpl(
                userProfileMapper,
                accountMapper,
                privacyPolicyService,
                userPrivacySettingService,
                userRelationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", userProfileMapper);
        return service;
    }

    private Account account(Integer accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        return account;
    }

    private UserProfile userProfile(Integer accountId) {
        return UserProfile.builder()
                .accountId(accountId)
                .build();
    }

    private UserPrivacySetting setting() {
        return UserPrivacySetting.builder()
                .accountId(18)
                .birthdayVisibility(VisibilityScope.PUBLIC)
                .build();
    }
}
