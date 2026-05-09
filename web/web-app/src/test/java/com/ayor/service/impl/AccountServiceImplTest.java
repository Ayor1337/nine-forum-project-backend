package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.pojo.Account;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountInfoMapper accountInfoMapper;

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
    private UserRelationService userRelationService;

    @Mock
    private PrivacyPolicyService privacyPolicyService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Mock
    private AccountInfoService accountInfoService;

    @Mock
    private ImageStorageService imageStorageService;

    @Test
    void shouldUploadAvatarThroughImageStorageService() throws Exception {
        AccountServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(7);
        Base64Upload upload = new Base64Upload("data:image/png;base64,abc", "avatar.png");
        StoredImage storedImage = createStoredImage("nineforum/avatar/test.png");

        when(accountMapper.selectById(7)).thenReturn(account);
        when(imageStorageService.storeImageBase64Image(upload, "avatar/")).thenReturn(storedImage);
        when(accountMapper.updateById(account)).thenReturn(1);

        String result = service.updateUserAvatar(7, upload);

        assertNull(result);
        assertEquals("nineforum/avatar/test.png", account.getAvatarUrl());
        verify(imageStorageService).storeImageBase64Image(upload, "avatar/");
        verify(minioService, never()).uploadBase64(upload, "avatar/");
    }

    @Test
    void shouldUploadBannerThroughImageStorageService() throws Exception {
        AccountServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(7);
        Base64Upload upload = new Base64Upload("data:image/png;base64,abc", "banner.png");
        StoredImage storedImage = createStoredImage("nineforum/banner/test.png");

        when(accountMapper.selectById(7)).thenReturn(account);
        when(imageStorageService.storeImageBase64Image(upload, "banner/")).thenReturn(storedImage);
        when(accountMapper.updateById(account)).thenReturn(1);

        String result = service.updateUserBanner(7, upload);

        assertNull(result);
        assertEquals("nineforum/banner/test.png", account.getBannerUrl());
        verify(imageStorageService).storeImageBase64Image(upload, "banner/");
        verify(minioService, never()).uploadBase64(upload, "banner/");
    }

    private AccountServiceImpl createService() {
        AccountServiceImpl service = new AccountServiceImpl(
                accountMapper,
                accountInfoMapper,
                permissionMapper,
                roleMapper,
                passwordEncoder,
                accountStatMapper,
                jwtUtils,
                passwordEncoder,
                userRelationService,
                privacyPolicyService,
                userPrivacySettingService,
                accountInfoService,
                imageStorageService
        );
        ReflectionTestUtils.setField(service, "baseMapper", accountMapper);
        return service;
    }

    private StoredImage createStoredImage(String url) {
        StoredImage image = new StoredImage();
        image.setUrl(url);
        image.setObjectName("unused");
        image.setOriginalExt("png");
        image.setOutputExt("png");
        image.setMimeType("image/png");
        image.setFileSize(123L);
        image.setWidth(16);
        image.setHeight(16);
        image.setSha256("hash");
        image.setBytes(new byte[]{1, 2, 3});
        return image;
    }
}
