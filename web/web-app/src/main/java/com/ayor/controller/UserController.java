package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.UserProfileDTO;
import com.ayor.entity.dto.PasswordChangeDTO;
import com.ayor.entity.dto.UserReportDTO;
import com.ayor.entity.dto.UserPrivacySettingDTO;
import com.ayor.entity.vo.AccountStatVO;
import com.ayor.entity.vo.UserProfileVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.entity.vo.UserPrivacySettingVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AccountStatService;
import com.ayor.service.ReportService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AccountService accountService;

    private final AccountStatService accountStatService;

    private final SecurityUtils security;

    private final UserPrivacySettingService userPrivacySettingService;

    private final UserRelationService userRelationService;

    private final ReportService reportService;
    /**
     * 获取当前登录用户的资料。
     *
     * @return 当前登录用户资料
     */
    @GetMapping("/me")
    public Result<UserInfoVO> getUserInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }

    /**
     * 根据用户 ID 获取公开资料。
     *
     * @param userId 目标用户ID
     * @return 公开用户资料
     */
    @GetMapping("/{user_id}")
    public Result<UserInfoVO> getUserInfoByUserId(@PathVariable("user_id") Integer userId) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getPublicUserInfo(viewerId, userId), "获取用户信息失败,用户可能不存在");
    }

    /**
     * 根据用户 ID 获取统计概览。
     *
     * @param userId 目标用户ID
     * @return 用户统计数据，包含帖子、主题等聚合信息
     */
    @GetMapping("/{user_id}/stats")
    public Result<AccountStatVO> getAccountStatInfo(@PathVariable("user_id") Integer userId) {
        return Result.dataMessageHandler(() -> accountStatService.getAccountStatByUserId(userId), "获取用户统计信息失败");
    }

    /**
     * 获取指定用户的粉丝列表。
     *
     * @param userId 目标用户ID
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @return 粉丝列表分页结果
     */
    @GetMapping("/{user_id}/followers")
    public Result<PageEntity<UserInfoVO>> getFollowers(@PathVariable("user_id") Integer userId,
                                                       @RequestParam("page") Integer page,
                                                       @RequestParam("page_size") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getFollowers(viewerId, userId, page, pageSize), "获取粉丝列表失败");
    }

    /**
     * 获取指定用户的关注列表。
     *
     * @param userId 目标用户ID
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @return 关注列表分页结果
     */
    @GetMapping("/{user_id}/followings")
    public Result<PageEntity<UserInfoVO>> getFollowings(@PathVariable("user_id") Integer userId,
                                                        @RequestParam("page") Integer page,
                                                        @RequestParam("page_size") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getFollowings(viewerId, userId, page, pageSize), "获取关注列表失败");
    }
    /**
     * 更新当前用户头像。
     *
     * @param dto 头像上传数据
     * @return 操作结果
     */
    @PutMapping("/me/avatar")
    public Result<Void> updateAvatar(@RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserAvatar(userId, dto));
    }

    /**
     * 更新用户个人资料。
     *
     * @param dto 个人资料请求体
     * @return 操作结果
     */
    @PutMapping("/me/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UserProfileDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserProfile(userId, dto));
    }

    @GetMapping("/me/profile")
    public Result<UserProfileVO> getMyProfile() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getMyProfile(userId), "获取用户资料失败");
    }

    @GetMapping("/{user_id}/profile")
    public Result<UserProfileVO> getPublicProfile(@PathVariable("user_id") Integer userId) {
        Integer viewerId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getPublicProfile(viewerId, userId), "获取用户资料失败");
    }

    /**
     * 获取当前登录用户的隐私设置。
     *
     * @return 当前用户隐私设置
     */
    @GetMapping("/me/privacy")
    public Result<UserPrivacySettingVO> getPrivacySetting() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> userPrivacySettingService.getPrivacySetting(userId), "获取隐私设置失败");
    }

    /**
     * 更新当前登录用户的隐私设置。
     *
     * @param dto 隐私设置请求体
     * @return 操作结果
     */
    @PutMapping("/me/privacy")
    public Result<Void> updatePrivacySetting(@RequestBody @Valid UserPrivacySettingDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> userPrivacySettingService.updatePrivacySetting(userId, dto));
    }

    /**
     * 关注指定用户。
     *
     * @param userId 被关注用户ID
     * @return 操作结果
     */
    @PostMapping("/{user_id}/follow")
    public Result<Void> follow(@PathVariable("user_id") Integer userId) {
        Integer currentUserId = security.getSecurityUserId();
        return Result.messageHandler(() -> userRelationService.follow(currentUserId, userId));
    }

    /**
     * 取消关注指定用户。
     *
     * @param userId 被取消关注用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{user_id}/follow")
    public Result<Void> unfollow(@PathVariable("user_id") Integer userId) {
        Integer currentUserId = security.getSecurityUserId();
        return Result.messageHandler(() -> userRelationService.unfollow(currentUserId, userId));
    }

    /**
     * 拉黑指定用户。
     *
     * @param userId 被拉黑用户ID
     * @return 操作结果
     */
    @PostMapping("/{user_id}/block")
    public Result<Void> block(@PathVariable("user_id") Integer userId) {
        Integer currentUserId = security.getSecurityUserId();
        return Result.messageHandler(() -> userRelationService.block(currentUserId, userId));
    }

    /**
     * 取消拉黑指定用户。
     *
     * @param userId 被取消拉黑用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{user_id}/block")
    public Result<Void> unblock(@PathVariable("user_id") Integer userId) {
        Integer currentUserId = security.getSecurityUserId();
        return Result.messageHandler(() -> userRelationService.unblock(currentUserId, userId));
    }

    @PostMapping("/{user_id}/reports")
    public Result<Void> createUserReport(@PathVariable("user_id") Integer userId,
                                         @RequestBody @Valid UserReportDTO dto) {
        Integer currentUserId = security.getSecurityUserId();
        return Result.messageHandler(() -> reportService.createUserReport(currentUserId, userId, dto));
    }

    /**
     * 更新当前用户横幅图。
     *
     * @param dto 横幅图上传数据
     * @return 操作结果
     */
    @PutMapping("/me/banner")
    public Result<Void> updateBanner(@RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserBanner(userId , dto));
    }

    /**
     * 通过旧密码更新当前账号密码。
     *
     * @param dto 密码修改请求体
     * @param request 当前请求
     * @return 操作结果
     */
    @PostMapping("/me/password")
    public Result<Void> updatePassword(@RequestBody PasswordChangeDTO dto,
                                       HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return Result.messageHandler(() -> accountService.updatePasswordWithOld(token, dto));
    }

}
