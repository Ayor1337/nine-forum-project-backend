package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.dto.AccountProfileDTO;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AccountService accountService;

    private final SecurityUtils security;
    /**
     * 获取当前登录用户的资料。
     */

    @GetMapping("/me")
    public Result<UserInfoVO> getUserInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }
    /**
     * 根据用户 ID 获取公开资料。
     */

    @GetMapping("/{user_id}")
    public Result<UserInfoVO> getUserInfoByUserId(@PathVariable("user_id") Integer userId) {
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }
    /**
     * 更新当前用户头像。
     */

    @PutMapping("/me/avatar")
    public Result<Void> updateAvatar(@RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserAvatar(userId, dto));
    }

    /**
     * 更新用户个人资料
     */
    @PutMapping("/me/profile")
    public Result<Void> updateProfile(@RequestBody @Valid AccountProfileDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserProfile(userId, dto));
    }

    /**
     * 更新当前用户横幅图。
     */

    @PutMapping("/me/banner")
    public Result<Void> updateBanner( @RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserBanner(userId , dto));
    }

}
