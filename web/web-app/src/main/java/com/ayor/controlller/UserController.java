package com.ayor.controlller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AccountService accountService;

    private final SecurityUtils security;

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountService.getUserInfo(userId), "获取用户信息失败,用户可能不存在");
    }

    @GetMapping("/info/by_user_id")
    public Result<UserInfoVO> getUserInfoByUserId(@RequestParam("user_id") String userId) {
        return Result.dataMessageHandler(() -> accountService.getUserInfo(Integer.parseInt(userId)), "获取用户信息失败,用户可能不存在");
    }

    @PutMapping("/update_avatar")
    public Result<Void> updateAvatar(@RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserAvatar(userId, dto));
    }

    @PutMapping("/update_banner")
    public Result<Void> updateBanner( @RequestBody Base64Upload dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> accountService.updateUserBanner(userId , dto));
    }

}
