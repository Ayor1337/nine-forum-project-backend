package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.entity.vo.AccountStatVO;
import com.ayor.result.Result;
import com.ayor.service.AccountStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account_stats")
@RequiredArgsConstructor
public class AccountStatController {

    private final AccountStatService accountStatService;

    /**
     * 分页查询用户统计记录，可按用户过滤。
     */
    @GetMapping
    public Result<PageEntity<AccountStatVO>> listAccountStats(@RequestParam("page_num") Integer pageNum,
                                                              @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                              @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> accountStatService.getAccountStats(pageNum, pageSize, accountId), "获取用户统计失败");
    }

    /**
     * 查询单条用户统计记录。
     */
    @GetMapping("/{statId}")
    public Result<AccountStatVO> getAccountStat(@PathVariable("statId") Integer statId) {
        return Result.dataMessageHandler(() -> accountStatService.getAccountStatById(statId), "获取用户统计失败");
    }

    /**
     * 创建用户统计记录。
     */
    @PostMapping
    public Result<Void> createAccountStat(@RequestBody AccountStat accountStat) {
        return Result.messageHandler(() -> accountStatService.createAccountStat(accountStat));
    }

    /**
     * 更新指定用户统计记录。
     */
    @PutMapping("/{statId}")
    public Result<Void> updateAccountStat(@PathVariable("statId") Integer statId,
                                          @RequestBody AccountStat accountStat) {
        return Result.messageHandler(() -> accountStatService.updateAccountStat(statId, accountStat));
    }

    /**
     * 删除指定用户统计记录。
     */
    @DeleteMapping("/{statId}")
    public Result<Void> deleteAccountStat(@PathVariable("statId") Integer statId) {
        return Result.messageHandler(() -> accountStatService.deleteAccountStat(statId));
    }
}
