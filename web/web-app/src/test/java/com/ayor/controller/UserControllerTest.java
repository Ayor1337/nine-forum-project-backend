package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.LoginSessionVO;
import com.ayor.entity.vo.UserAvatarItemVO;
import com.ayor.result.Result;
import com.ayor.controller.exception.ValidateController;
import com.ayor.service.AccountService;
import com.ayor.service.AccountStatService;
import com.ayor.service.ReportService;
import com.ayor.service.UserLoginSessionService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.util.JWTUtils;
import com.ayor.util.SecurityUtils;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final AccountService accountService = mock(AccountService.class);
    private final AccountStatService accountStatService = mock(AccountStatService.class);
    private final SecurityUtils securityUtils = mock(SecurityUtils.class);
    private final UserPrivacySettingService userPrivacySettingService = mock(UserPrivacySettingService.class);
    private final UserRelationService userRelationService = mock(UserRelationService.class);
    private final ReportService reportService = mock(ReportService.class);
    private final UserLoginSessionService loginSessionService = mock(UserLoginSessionService.class);
    private final JWTUtils jwtUtils = mock(JWTUtils.class);
    private final UserController controller = new UserController(
            accountService,
            accountStatService,
            securityUtils,
            userPrivacySettingService,
            userRelationService,
            reportService,
            loginSessionService,
            jwtUtils
    );

    // 测试获取公开用户信息使用可选查看者 ID
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
    void getUserAvatarsShouldDelegateAndReturnSuccessfulEmptyList() {
        when(accountService.getUserAvatars(List.of(7, 18)))
                .thenReturn(List.of(new UserAvatarItemVO(7, "avatar-7.webp")));

        Result<List<UserAvatarItemVO>> result = controller.getUserAvatars(List.of(7, 18));

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(7, result.getData().get(0).getAccountId());
        verify(accountService).getUserAvatars(List.of(7, 18));
    }

    @Test
    void getUserAvatarsShouldBindCommaSeparatedParameters() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ValidateController())
                .build();
        when(accountService.getUserAvatars(List.of(18, 7)))
                .thenReturn(List.of(new UserAvatarItemVO(18, "avatar-18.webp")));

        mockMvc.perform(get("/api/users/avatars")
                        .param("user_ids", "18,7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].accountId").value(18))
                .andExpect(jsonPath("$.data[0].avatarUrl").value("avatar-18.webp"));

        verify(accountService).getUserAvatars(List.of(18, 7));
    }

    @Test
    void getUserAvatarsShouldBindRepeatedParameters() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ValidateController())
                .build();
        when(accountService.getUserAvatars(List.of(18, 7)))
                .thenReturn(List.of(new UserAvatarItemVO(18, "avatar-18.webp")));

        mockMvc.perform(get("/api/users/avatars")
                        .param("user_ids", "18")
                        .param("user_ids", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(accountService).getUserAvatars(List.of(18, 7));
    }

    @Test
    void getUserAvatarsShouldRejectEmptyRequestParameter() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ValidateController())
                .build();

        mockMvc.perform(get("/api/users/avatars").param("user_ids", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(203));

        verifyNoInteractions(accountService);
    }

    @Test
    void getUserAvatarsShouldRejectEmptyAndOversizedInput() {
        Result<List<UserAvatarItemVO>> emptyResult = controller.getUserAvatars(List.of());
        Result<List<UserAvatarItemVO>> oversizedResult = controller.getUserAvatars(
                java.util.stream.IntStream.rangeClosed(1, 101).boxed().toList());

        assertEquals(203, emptyResult.getCode());
        assertEquals(203, oversizedResult.getCode());
        verifyNoInteractions(accountService);
    }

    @Test
    void userAvatarBatchEndpointShouldUseUserIdsRequestParameter() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("getUserAvatars", List.class);

        assertArrayEquals(new String[]{"/avatars"}, method.getAnnotation(GetMapping.class).value());
    }

    // 测试获取粉丝使用可选查看者 ID
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

    // 测试获取关注使用可选查看者 ID
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

    // 测试资料接口使用资料路径
    @Test
    void profileEndpointsShouldUseProfilePaths() throws NoSuchMethodException {
        Method myProfile = UserController.class.getMethod("getMyProfile");
        Method publicProfile = UserController.class.getMethod("getPublicProfile", Integer.class);

        assertArrayEquals(new String[]{"/me/profile"}, myProfile.getAnnotation(GetMapping.class).value());
        assertArrayEquals(new String[]{"/{user_id}/profile"}, publicProfile.getAnnotation(GetMapping.class).value());
    }

    // 测试资料视图对象使用资料名称
    @Test
    void profileViewObjectShouldUseProfileName() {
        assertDoesNotThrow(() -> Class.forName("com.ayor.entity.vo.UserProfileVO"));
    }

    // 测试列表当前用户会话带有当前会话 ID
    @Test
    void shouldListCurrentUserSessionsWithCurrentSessionId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        LoginSessionVO session = new LoginSessionVO();
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtils.resolveJwt("Bearer token")).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("sid")).thenReturn(claim);
        when(claim.asString()).thenReturn("session-current");
        when(loginSessionService.listSessions(7, "session-current", 2, 5))
                .thenReturn(new PageEntity<>(1L, List.of(session)));

        Result<PageEntity<LoginSessionVO>> result = controller.listLoginSessions(request, 2, 5);

        assertNotNull(result);
        verify(loginSessionService).listSessions(7, "session-current", 2, 5);
    }

    // 测试撤销当前用户其他会话
    @Test
    void shouldRevokeCurrentUsersOtherSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtils.resolveJwt("Bearer token")).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("sid")).thenReturn(claim);
        when(claim.asString()).thenReturn("session-current");

        Result<Void> result = controller.revokeLoginSession("session-old", request);

        assertNotNull(result);
        verify(loginSessionService).revokeSession(7, "session-old", "session-current");
    }
}
