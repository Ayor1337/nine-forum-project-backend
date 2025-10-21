package com.ayor.controlller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.util.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
        String username = security.getSecurityUsername();
        return Result.dataMessageHandler(() -> accountService.getUserInfo(username), "获取用户信息失败,用户可能不存在");
    }

    @GetMapping("/info/by_user_id")
    public Result<UserInfoVO> getUserInfoByUserId(@RequestParam("user_id") String userId) {
        return Result.dataMessageHandler(() -> accountService.getUserInfoById(Integer.parseInt(userId)), "获取用户信息失败,用户可能不存在");
    }

    @PutMapping("/update_avatar")
    public Result<Void> updateAvatar(@RequestBody Base64Upload dto) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> accountService.updateUserAvatar(username, dto));
    }

    @PutMapping("/update_banner")
    public Result<Void> updateBanner( @RequestBody Base64Upload dto) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> accountService.updateUserBanner(username , dto));
    }

}
