package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AccountStatService;
import com.ayor.service.ReportService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final AccountService accountService = mock(AccountService.class);
    private final AccountStatService accountStatService = mock(AccountStatService.class);
    private final SecurityUtils securityUtils = mock(SecurityUtils.class);
    private final UserPrivacySettingService userPrivacySettingService = mock(UserPrivacySettingService.class);
    private final UserRelationService userRelationService = mock(UserRelationService.class);
    private final ReportService reportService = mock(ReportService.class);
    private final UserController controller = new UserController(
            accountService,
            accountStatService,
            securityUtils,
            userPrivacySettingService,
            userRelationService,
            reportService
    );

    @Test
    void getPublicUserInfoShouldUseOptionalViewerId() {
        UserInfoVO userInfo = new UserInfoVO();
        when(securityUtils.getOptionalSecurityUserId()).thenReturn(null);
        when(accountService.getPublicUserInfo(null, 18)).thenReturn(userInfo);

        Result<UserInfoVO> result = controller.getUserInfoByUserId(18);

        assertNotNull(result);
        verify(securityUtils).getOptionalSecurityUserId();
        verify(accountService).getPublicUserInfo(null, 18);
    }

    @Test
    void getFollowersShouldUseOptionalViewerId() {
        PageEntity<UserInfoVO> page = new PageEntity<>(0L, List.of());
        when(securityUtils.getOptionalSecurityUserId()).thenReturn(null);
        when(accountService.getFollowers(null, 18, 1, 20)).thenReturn(page);

        Result<PageEntity<UserInfoVO>> result = controller.getFollowers(18, 1, 20);

        assertNotNull(result);
        verify(securityUtils).getOptionalSecurityUserId();
        verify(accountService).getFollowers(null, 18, 1, 20);
    }

    @Test
    void getFollowingsShouldUseOptionalViewerId() {
        PageEntity<UserInfoVO> page = new PageEntity<>(0L, List.of());
        when(securityUtils.getOptionalSecurityUserId()).thenReturn(null);
        when(accountService.getFollowings(null, 18, 1, 20)).thenReturn(page);

        Result<PageEntity<UserInfoVO>> result = controller.getFollowings(18, 1, 20);

        assertNotNull(result);
        verify(securityUtils).getOptionalSecurityUserId();
        verify(accountService).getFollowings(null, 18, 1, 20);
    }

    @Test
    void profileEndpointsShouldUseProfilePaths() throws NoSuchMethodException {
        Method myProfile = UserController.class.getMethod("getMyProfile");
        Method publicProfile = UserController.class.getMethod("getPublicProfile", Integer.class);

        assertArrayEquals(new String[]{"/me/profile"}, myProfile.getAnnotation(GetMapping.class).value());
        assertArrayEquals(new String[]{"/{user_id}/profile"}, publicProfile.getAnnotation(GetMapping.class).value());
    }

    @Test
    void profileViewObjectShouldUseProfileName() {
        assertDoesNotThrow(() -> Class.forName("com.ayor.entity.vo.UserProfileVO"));
    }
}
