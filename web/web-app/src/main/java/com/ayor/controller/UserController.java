package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AccountService accountService;

    private final SecurityUtils security;
    /**
     * getUserInfo 方法。
     */

    @GetMapping("/me")
    public Result<UserInfoVO> getUserInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }
    /**
     * getUserInfoByUserId 方法。
     */

    @GetMapping("/{user_id}")
    public Result<UserInfoVO> getUserInfoByUserId(@PathVariable("user_id") Integer userId) {
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }
    /**
     * updateAvatar 方法。
     */

    @PutMapping("/me/avatar")
    public Result<Void> updateAvatar(@RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserAvatar(userId, dto));
    }
    /**
     * updateBanner 方法。
     */

    @PutMapping("/me/banner")
    public Result<Void> updateBanner( @RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserBanner(userId , dto));
    }

}
