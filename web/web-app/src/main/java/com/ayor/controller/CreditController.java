package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.result.Result;
import com.ayor.service.CreditService;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
