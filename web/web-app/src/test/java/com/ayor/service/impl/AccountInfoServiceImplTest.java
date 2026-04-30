package com.ayor.service.impl;

import com.ayor.entity.app.vo.AccountInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.AccountInfo;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.AccountInfoMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountInfoServiceImplTest {

    @Mock
    private AccountInfoMapper accountInfoMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Mock
    private UserRelationService userRelationService;

    @InjectMocks
    private AccountInfoServiceImpl accountInfoService;

    @Test
    void shouldReturnBirthdayWhenPublicVisibilityAllows() {
        Date birthday = new Date();
        when(accountMapper.getAccountById(1)).thenReturn(new Account(1, "u1", null, null, null, null, 1, null, null, 3, false, null));
        when(accountInfoMapper.selectById(1)).thenReturn(AccountInfo.builder().accountId(1).birthday(birthday).bio("bio").build());
        when(privacyPolicyService.canViewProfile(2, 1)).thenReturn(true);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(privacySetting(VisibilityScope.PUBLIC));

        AccountInfoVO result = accountInfoService.getPublicAccountInfo(2, 1);

        assertNotNull(result);
        assertEquals(birthday, result.getBirthday());
    }

    @Test
    void shouldHideBirthdayWhenVisibilityDisallows() {
        Date birthday = new Date();
        when(accountMapper.getAccountById(1)).thenReturn(new Account(1, "u1", null, null, null, null, 1, null, null, 3, false, null));
        when(accountInfoMapper.selectById(1)).thenReturn(AccountInfo.builder().accountId(1).birthday(birthday).bio("bio").build());
        when(privacyPolicyService.canViewProfile(2, 1)).thenReturn(true);
        when(userPrivacySettingService.getByAccountId(1)).thenReturn(privacySetting(VisibilityScope.PRIVATE));

        AccountInfoVO result = accountInfoService.getPublicAccountInfo(2, 1);

        assertNotNull(result);
        assertNull(result.getBirthday());
    }

    private UserPrivacySetting privacySetting(VisibilityScope birthdayVisibility) {
        return UserPrivacySetting.builder()
                .accountId(1)
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PUBLIC)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(birthdayVisibility)
                .dmPermission(DmPermission.EVERYONE)
                .build();
    }
}
