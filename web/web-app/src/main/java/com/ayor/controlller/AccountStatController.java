package com.ayor.controlller;

import com.ayor.entity.app.vo.AccountStatVO;
import com.ayor.result.Result;
import com.ayor.service.AccountStatService;
import com.ayor.util.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stat")
public class AccountStatController {

    @Resource
    private AccountStatService accountStatService;

    @Resource
    private SecurityUtils security;

    @RequestMapping("/info")
    public Result<AccountStatVO> getAccountStatInfo() {
        String username = security.getSecurityUsername();
        return Result.dataMessageHandler(() -> accountStatService.getAccountStatByUsername(username), "获取用户统计信息失败");
    }
}
