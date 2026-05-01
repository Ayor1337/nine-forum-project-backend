package com.ayor.service.impl;

import com.ayor.entity.dto.AccountProfileDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.AccountInfo;
import com.ayor.mapper.AccountInfoMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.AccountInfoService;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private MinioService minioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountStatMapper accountStatMapper;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Mock
    private AccountInfoMapper accountInfoMapper;

    @Mock
    private AccountInfoService accountInfoService;

    @Spy
    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldUpdateNicknameAndExtendedProfileInAccountInfo() {
        Account account = new Account(1, "u1", "pwd", "old-name", null, null, 1, new Date(), new Date(), 3, false, "u1@test.com");
        AccountInfo accountInfo = AccountInfo.builder()
                .accountId(1)
                .bio("old bio")
                .build();
        AccountProfileDTO dto = AccountProfileDTO.builder()
                .nickname("new-name")
                .bio("new bio")
                .location("Taipei")
                .birthday(new Date())
                .website("https://example.com")
                .build();

        when(accountMapper.getAccountById(1)).thenReturn(account);
        when(accountInfoService.initDefaultIfAbsent(1)).thenReturn(accountInfo);
        doReturn(true).when(accountService).updateById(account);
        when(accountInfoMapper.updateById(any(AccountInfo.class))).thenReturn(1);

        String result = accountService.updateUserProfile(1, dto);

        assertNull(result);
        assertEquals("new-name", account.getNickname());
        assertEquals("new bio", accountInfo.getBio());
        assertEquals("Taipei", accountInfo.getLocation());
        assertEquals("https://example.com", accountInfo.getWebsite());
        verify(accountInfoMapper).updateById(accountInfo);
    }

    @Test
    void shouldCreateAccountInfoWhenMissingDuringProfileUpdate() {
        Account account = new Account(1, "u1", "pwd", "old-name", null, null, 1, new Date(), new Date(), 3, false, "u1@test.com");
        AccountProfileDTO dto = AccountProfileDTO.builder()
                .bio("new bio")
                .location("Kaohsiung")
                .build();

        AccountInfo newAccountInfo = AccountInfo.builder().accountId(1).build();
        when(accountMapper.getAccountById(1)).thenReturn(account);
        when(accountInfoService.initDefaultIfAbsent(1)).thenReturn(newAccountInfo);
        doReturn(true).when(accountService).updateById(account);
        when(accountInfoMapper.updateById(any(AccountInfo.class))).thenReturn(1);

        String result = accountService.updateUserProfile(1, dto);

        assertNull(result);
        verify(accountInfoMapper).updateById(any(AccountInfo.class));
    }
}
