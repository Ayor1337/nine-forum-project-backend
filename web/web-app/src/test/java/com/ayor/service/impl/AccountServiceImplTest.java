package com.ayor.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.UserAvatarItemVO;
import com.ayor.entity.vo.UserAvatarVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.UserItemVO;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.UserProfileMapper;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.UserItemMapper;
import com.ayor.service.UserProfileService;
import com.ayor.service.PrivacyPolicyService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.service.CacheInvalidationService;
import com.ayor.service.RegistrationVerificationGate;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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
    private UserItemMapper userItemMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    @Mock
    private RegistrationVerificationGate registrationVerificationGate;

    // 测试通过图片存储服务上传头像
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
    }

    // 测试通过图片存储服务上传横幅
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
    }

    // 测试获取公开用户信息填充关系字段用于查看者
    @Test
    void getPublicUserInfoShouldFillRelationFieldsForViewer() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);
        target.setUsername("target");

        when(accountMapper.selectById(18)).thenReturn(target);
        when(userRelationService.isFollowing(7, 18)).thenReturn(true);
        when(userRelationService.isFollowing(18, 7)).thenReturn(false);
        when(userRelationService.isBlocked(7, 18)).thenReturn(true);
        when(userRelationService.isBlocked(18, 7)).thenReturn(false);

        UserInfoVO result = service.getPublicUserInfo(7, 18);

        assertEquals(Boolean.TRUE, result.getIsFollowing());
        assertEquals(Boolean.FALSE, result.getIsFollowed());
        assertEquals(Boolean.TRUE, result.getIsBlock());
        assertEquals(Boolean.FALSE, result.getIsBlocked());
        verify(userRelationService).isFollowing(7, 18);
        verify(userRelationService).isFollowing(18, 7);
        verify(userRelationService).isBlocked(7, 18);
        verify(userRelationService).isBlocked(18, 7);
    }

    // 测试匿名查看公开用户信息时关系字段为空
    @Test
    void getPublicUserInfoShouldLeaveRelationFieldsNullForAnonymousViewer() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);

        when(accountMapper.selectById(18)).thenReturn(target);

        UserInfoVO result = service.getPublicUserInfo(null, 18);

        assertNull(result.getIsFollowing());
        assertNull(result.getIsFollowed());
        assertNull(result.getIsBlock());
        assertNull(result.getIsBlocked());
        verifyNoInteractions(userRelationService);
    }

    // 测试获取公开用户信息返回基础信息当资料可见性拒绝查看者
    @Test
    void getPublicUserInfoShouldReturnBasicInfoWhenProfileVisibilityDeniesViewer() {
        AccountServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(18);
        target.setUsername("target");
        target.setNickname("Target");
        target.setAvatarUrl("avatar.png");
        target.setBannerUrl("banner.png");

        when(accountMapper.selectById(18)).thenReturn(target);

        UserInfoVO result = service.getPublicUserInfo(7, 18);

        assertNotNull(result);
        assertEquals(18, result.getAccountId());
        assertEquals("target", result.getUsername());
        assertEquals("Target", result.getNickname());
        assertEquals("avatar.png", result.getAvatarUrl());
        assertEquals("banner.png", result.getBannerUrl());
        assertNull(result.getPermission());
        verify(privacyPolicyService, never()).canViewProfile(7, 18);
    }

    // 测试获取粉丝填充关系字段用于每个列表项
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

    // 测试获取关注填充关系字段用于每个列表项
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

    // 测试获取用户头像与已装备头像框、徽章
    @Test
    void getUserAvatarShouldReturnAvatarAndEquippedDecorations() {
        AccountServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(7);
        account.setAvatarUrl("https://example.com/avatar/7.webp");
        UserItemVO frame = new UserItemVO();
        frame.setItemKey("star_track_frame");
        frame.setName("头像框·星轨");
        frame.setDecorationConfig("{\"schemaVersion\": 2, \"mode\": \"css\"}");
        UserItemVO badge = new UserItemVO();
        badge.setItemKey("gold_medal");
        badge.setName("金质勋章");
        badge.setDecorationConfig("{\"schemaVersion\": 2, \"mode\": \"icon\", \"iconKey\": \"medal\"}");

        when(accountMapper.selectById(7)).thenReturn(account);
        when(userItemMapper.selectEquippedAvatarFrame(7)).thenReturn(frame);
        when(userItemMapper.selectEquippedBadge(7)).thenReturn(badge);

        UserAvatarVO result = service.getUserAvatar(7);

        assertEquals("https://example.com/avatar/7.webp", result.getAvatarUrl());
        assertEquals("star_track_frame", result.getAvatarFrameKey());
        assertEquals("头像框·星轨", result.getAvatarFrameName());
        assertEquals("gold_medal", result.getBadgeKey());
        assertEquals("金质勋章", result.getBadgeName());
        assertEquals("{\"schemaVersion\": 2, \"mode\": \"css\"}", result.getAvatarFrameConfig());
        assertEquals("{\"schemaVersion\": 2, \"mode\": \"icon\", \"iconKey\": \"medal\"}", result.getBadgeConfig());
    }

    // 测试用户未装备装饰时字段为空
    @Test
    void getUserAvatarShouldLeaveDecorationNullWhenNotEquipped() {
        AccountServiceImpl service = createService();
        Account account = new Account();
        account.setAccountId(7);
        account.setAvatarUrl("https://example.com/avatar/7.webp");

        when(accountMapper.selectById(7)).thenReturn(account);
        when(userItemMapper.selectEquippedAvatarFrame(7)).thenReturn(null);

        UserAvatarVO result = service.getUserAvatar(7);

        assertEquals("https://example.com/avatar/7.webp", result.getAvatarUrl());
        assertNull(result.getAvatarFrameKey());
        assertNull(result.getAvatarFrameName());
        assertNull(result.getBadgeKey());
        assertNull(result.getBadgeName());
        assertNull(result.getAvatarFrameConfig());
        assertNull(result.getBadgeConfig());
    }

    // 测试用户不存在时返回 null
    @Test
    void getUserAvatarShouldReturnNullWhenAccountMissing() {
        AccountServiceImpl service = createService();
        when(accountMapper.selectById(99)).thenReturn(null);

        UserAvatarVO result = service.getUserAvatar(99);

        assertNull(result);
        verify(userItemMapper, never()).selectEquippedAvatarFrame(99);
        verify(userItemMapper, never()).selectEquippedBadge(99);
    }

    @Test
    void getUserAvatarsShouldQueryOnceAndKeepFirstRequestOrder() {
        AccountServiceImpl service = createService();
        Account account7 = new Account();
        account7.setAccountId(7);
        account7.setAvatarUrl("https://example.com/avatar/7.webp");
        Account account18 = new Account();
        account18.setAccountId(18);
        account18.setAvatarUrl("https://example.com/avatar/18.webp");
        when(accountMapper.getAccountsByIds(List.of(18, 7, 99))).thenReturn(List.of(account7, account18));

        List<UserAvatarItemVO> result = service.getUserAvatars(List.of(18, 7, 18, 99));

        assertEquals(2, result.size());
        assertEquals(18, result.get(0).getAccountId());
        assertEquals("https://example.com/avatar/18.webp", result.get(0).getAvatarUrl());
        assertEquals(7, result.get(1).getAccountId());
        assertEquals("https://example.com/avatar/7.webp", result.get(1).getAvatarUrl());
        verify(accountMapper).getAccountsByIds(List.of(18, 7, 99));
    }

    @Test
    void getUserAvatarsShouldReturnEmptyWithoutQueryForEmptyInput() {
        AccountServiceImpl service = createService();

        List<UserAvatarItemVO> result = service.getUserAvatars(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(accountMapper);
    }

    @Test
    void insertNewAccountRejectsAlreadyConsumedTokenBeforePersistence() {
        AccountServiceImpl service = createService();
        AccountDTO dto = AccountDTO.builder()
                .username("tester")
                .password("secret1")
                .nickname("Tester")
                .token("consumed-token")
                .build();
        when(jwtUtils.consumeEmailJwt("consumed-token")).thenReturn(null);

        assertEquals("验证失败", service.insertNewAccount(dto));

        verify(accountMapper, never()).insert(any(Account.class));
        verifyNoInteractions(registrationVerificationGate);
    }

    @Test
    void insertNewAccountConsumesTokenAndClearsMatchingIdempotencyAfterSuccess() {
        AccountServiceImpl service = createService();
        AccountDTO dto = AccountDTO.builder()
                .username("tester")
                .password("secret1")
                .nickname("Tester")
                .token("valid-token")
                .build();
        DecodedJWT decodedJWT = org.mockito.Mockito.mock(DecodedJWT.class);
        Claim emailClaim = org.mockito.Mockito.mock(Claim.class);
        when(jwtUtils.consumeEmailJwt("valid-token")).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("email")).thenReturn(emailClaim);
        when(emailClaim.asString()).thenReturn("user@example.com");
        when(decodedJWT.getId()).thenReturn("jwt-id");
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(accountMapper.insert(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setAccountId(7);
            return 1;
        });
        when(accountStatMapper.insertNewAccountStat(7)).thenReturn(true);

        assertNull(service.insertNewAccount(dto));

        verify(jwtUtils).consumeEmailJwt("valid-token");
        verify(userProfileService).initDefaultIfAbsent(7);
        verify(userPrivacySettingService).initDefaultIfAbsent(7);
        verify(registrationVerificationGate).complete("user@example.com", "jwt-id");
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
                userItemMapper,
                imageStorageService,
                cacheInvalidationService,
                registrationVerificationGate
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
