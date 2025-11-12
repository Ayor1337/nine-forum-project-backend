package com.ayor.controlller;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.result.Result;
import com.ayor.service.AccountStatService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stat")
public class AccountStatController {

    private final AccountStatService accountStatService;


    private final SecurityUtils security;


    @RequestMapping("/info")
    public Result<AccountStatVO> getAccountStatInfo() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> accountStatService.getAccountStatByUsername(userId), "获取用户统计信息失败");
    }
}
