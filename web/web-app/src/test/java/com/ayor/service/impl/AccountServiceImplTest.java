package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.UserProfileService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

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
    private UserProfileService userProfileService;

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

    @Test
    void getPublicUserInfoShouldFillRelationFieldsForViewer() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);
        target.setUsername("target");

        when(accountMapper.selectById(18)).thenReturn(target);
        when(privacyPolicyService.canViewProfile(7, 18)).thenReturn(true);
        when(userRelationService.isFollowing(7, 18)).thenReturn(true);
        when(userRelationService.isFollowing(18, 7)).thenReturn(false);

        UserInfoVO result = service.getPublicUserInfo(7, 18);

        assertEquals(Boolean.TRUE, result.getIsFollowing());
        assertEquals(Boolean.FALSE, result.getIsFollowed());
        verify(userRelationService).isFollowing(7, 18);
        verify(userRelationService).isFollowing(18, 7);
    }

    @Test
    void getPublicUserInfoShouldLeaveRelationFieldsNullForAnonymousViewer() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);

        when(accountMapper.selectById(18)).thenReturn(target);
        when(privacyPolicyService.canViewProfile(null, 18)).thenReturn(true);

        UserInfoVO result = service.getPublicUserInfo(null, 18);

        assertNull(result.getIsFollowing());
        assertNull(result.getIsFollowed());
        verifyNoInteractions(userRelationService);
    }

    @Test
    void getFollowersShouldFillRelationFieldsForEachListItem() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);
        UserInfoVO follower = new UserInfoVO();
        follower.setAccountId(9);
        PageEntity<UserInfoVO> page = new PageEntity<>(1L, List.of(follower));

        when(accountMapper.selectById(18)).thenReturn(target);
        when(privacyPolicyService.canViewFollowerList(7, 18)).thenReturn(true);
        when(userRelationService.getFollowers(18, 1, 20)).thenReturn(page);
        when(userRelationService.isFollowing(7, 9)).thenReturn(false);
        when(userRelationService.isFollowing(9, 7)).thenReturn(true);

        PageEntity<UserInfoVO> result = service.getFollowers(7, 18, 1, 20);

        UserInfoVO item = result.getData().get(0);
        assertEquals(Boolean.FALSE, item.getIsFollowing());
        assertEquals(Boolean.TRUE, item.getIsFollowed());
    }

    @Test
    void getFollowingsShouldFillRelationFieldsForEachListItem() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);
        UserInfoVO following = new UserInfoVO();
        following.setAccountId(10);
        PageEntity<UserInfoVO> page = new PageEntity<>(1L, List.of(following));

        when(accountMapper.selectById(18)).thenReturn(target);
        when(privacyPolicyService.canViewFollowingList(7, 18)).thenReturn(true);
        when(userRelationService.getFollowings(18, 1, 20)).thenReturn(page);
        when(userRelationService.isFollowing(7, 10)).thenReturn(true);
        when(userRelationService.isFollowing(10, 7)).thenReturn(true);

        PageEntity<UserInfoVO> result = service.getFollowings(7, 18, 1, 20);

        UserInfoVO item = result.getData().get(0);
        assertEquals(Boolean.TRUE, item.getIsFollowing());
        assertEquals(Boolean.TRUE, item.getIsFollowed());
    }

    private AccountServiceImpl createService() {
        AccountServiceImpl service = new AccountServiceImpl(
                accountMapper,
                userProfileMapper,
                permissionMapper,
                roleMapper,
                passwordEncoder,
                accountStatMapper,
                jwtUtils,
                passwordEncoder,
                userRelationService,
                privacyPolicyService,
                userPrivacySettingService,
                userProfileService,
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
