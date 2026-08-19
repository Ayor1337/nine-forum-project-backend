package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.entity.vo.RecentCheckInUserVO;
import com.ayor.result.Result;
import com.ayor.service.CreditService;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/credits")
@Tag(name = "货币", description = "Credit 余额与流水查询接口")
public class CreditController {

    private final CreditService creditService;

    private final SecurityUtils security;

    /**
     * 查询当前用户的 Credit 余额。
     */
    @Operation(summary = "查询当前用户 Credit 余额")
    @GetMapping("/balance")
    public Result<CreditBalanceVO> getBalance() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> creditService.getBalance(userId), "获取余额失败");
    }

    /**
     * 当前用户每日签到并领取 Credit。
     */
    @Operation(summary = "每日签到领取 Credit")
    @PostMapping("/check-ins")
    public Result<Void> checkIn() {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> creditService.checkIn(userId));
    }

    /**
     * 查询最近完成签到的用户。
     */
    @Operation(summary = "查询最近签到用户")
    @GetMapping("/recent-check-ins")
    public Result<List<RecentCheckInUserVO>> listRecentCheckInUsers() {
        return Result.dataMessageHandler(creditService::listRecentCheckInUsers, "获取最近签到用户失败");
    }

    /**
     * 查询当前用户当天是否已签到。
     */
    @Operation(summary = "查询今日签到状态")
    @GetMapping("/check-ins/status")
    public Result<Boolean> getCheckInStatus() {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> creditService.hasCheckedInToday(userId), "获取签到状态失败");
    }

    /**
     * 分页查询当前用户的 Credit 流水。
     */
    @Operation(summary = "分页查询当前用户 Credit 流水")
    @GetMapping("/transactions")
    public Result<PageEntity<CreditTransactionVO>> listMyTransactions(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> creditService.listMyTransactions(userId, pageNum, pageSize), "获取流水失败");
    }
}
