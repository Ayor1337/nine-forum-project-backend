package com.ayor.service.impl;

import com.ayor.entity.dto.UserPrivacySettingDTO;
import com.ayor.entity.vo.UserPrivacySettingVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserPrivacySettingMapper;
import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPrivacySettingServiceImplTest {

    @Mock
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private UserPrivacySettingServiceImpl userPrivacySettingService;

    @Test
    void shouldCreateDefaultBirthdayVisibilityAsPrivate() {
        when(accountMapper.getAccountById(1)).thenReturn(new Account(1, "u1", null, null, null, null, 1, null, null, 3, false, null));
        when(userPrivacySettingMapper.insert(any(UserPrivacySetting.class))).thenReturn(1);

        UserPrivacySetting result = userPrivacySettingService.createDefault(1);

        assertNotNull(result);
        assertEquals(VisibilityScope.PRIVATE, result.getBirthdayVisibility());
    }

    @Test
    void shouldExposeBirthdayVisibilityInPrivacyView() {
        UserPrivacySetting setting = UserPrivacySetting.builder()
                .accountId(1)
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PRIVATE)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(VisibilityScope.FOLLOWER_ONLY)
                .dmPermission(DmPermission.EVERYONE)
                .build();
        when(accountMapper.getAccountById(1)).thenReturn(new Account(1, "u1", null, null, null, null, 1, null, null, 3, false, null));
        when(userPrivacySettingMapper.selectById(1)).thenReturn(setting);

        UserPrivacySettingVO result = userPrivacySettingService.getPrivacySetting(1);

        assertNotNull(result);
        assertEquals(VisibilityScope.FOLLOWER_ONLY, result.getBirthdayVisibility());
    }

    @Test
    void shouldUpdateBirthdayVisibility() {
        UserPrivacySetting setting = UserPrivacySetting.builder()
                .accountId(1)
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PRIVATE)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(VisibilityScope.PRIVATE)
                .dmPermission(DmPermission.EVERYONE)
                .build();
        UserPrivacySettingDTO dto = UserPrivacySettingDTO.builder()
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PRIVATE)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(VisibilityScope.PUBLIC)
                .dmPermission(DmPermission.EVERYONE)
                .build();

        when(accountMapper.getAccountById(1)).thenReturn(new Account(1, "u1", null, null, null, null, 1, null, null, 3, false, null));
        when(userPrivacySettingMapper.selectById(1)).thenReturn(setting);
        when(userPrivacySettingMapper.updateById(any(UserPrivacySetting.class))).thenReturn(1);

        userPrivacySettingService.updatePrivacySetting(1, dto);

        assertEquals(VisibilityScope.PUBLIC, setting.getBirthdayVisibility());
    }
}
