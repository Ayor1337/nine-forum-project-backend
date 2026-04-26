package com.ayor.controller;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.result.Result;
import com.ayor.service.AccountStatService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/me")
public class AccountStatController {

    private final AccountStatService accountStatService;


    private final SecurityUtils security;
    /**
     * 获取当前登录用户的统计概览。
     *
     * @return 用户统计数据，包含帖子、主题等聚合信息
     */


    @GetMapping("/stats")
    public Result<AccountStatVO> getAccountStatInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountStatService.getAccountStatByUsername(userId), "获取用户统计信息失败");
    }
}
