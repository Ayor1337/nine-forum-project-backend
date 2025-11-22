package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.result.Result;
import com.ayor.service.AccountStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account_stat")
@RequiredArgsConstructor
public class AccountStatController {

    private final AccountStatService accountStatService;

    @GetMapping("/list")
    public Result<PageEntity<AccountStat>> listAccountStats(@RequestParam("page_num") Integer pageNum,
                                                            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                            @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> accountStatService.getAccountStats(pageNum, pageSize, accountId), "获取用户统计失败");
    }

    @PutMapping("/{stat_id}")
    public Result<Void> updateAccountStat(@PathVariable("stat_id") Integer statId,
                                          @RequestBody AccountStat accountStat) {
        return Result.messageHandler(() -> accountStatService.updateAccountStat(statId, accountStat));
    }
}
