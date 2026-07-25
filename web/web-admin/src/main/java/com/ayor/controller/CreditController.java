package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.CreditAdjustDTO;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.result.Result;
import com.ayor.service.CreditService;
import com.ayor.type.CreditChangeType;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "货币管理", description = "后台 Credit 发放/扣减与查询接口")
public class CreditController {

    private final CreditService creditService;

    private final SecurityUtils securityUtils;

    @PostMapping("/adjustments")
    @Operation(summary = "调整用户 Credit（正数发放，负数扣减）")
    public Result<Void> adjustCredit(@RequestBody @Valid CreditAdjustDTO dto) {
        Integer operatorId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> creditService.adjustCredit(operatorId, dto));
    }

    @GetMapping("/{account_id}/balance")
    @Operation(summary = "查询指定用户 Credit 余额")
    public Result<CreditBalanceVO> getBalance(
            @Parameter(description = "用户账号ID") @PathVariable("account_id") Integer accountId) {
        return Result.dataMessageHandler(() -> creditService.getBalance(accountId), "用户不存在");
    }

    @GetMapping("/transactions")
    @Operation(summary = "分页查询 Credit 流水")
    public Result<PageEntity<CreditTransactionVO>> listTransactions(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "用户账号ID") @RequestParam(value = "account_id", required = false) Integer accountId,
            @Parameter(description = "用户名") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "变动类型") @RequestParam(value = "change_type", required = false) CreditChangeType changeType,
            @Parameter(description = "排序方向") @RequestParam(value = "sort_order", defaultValue = "desc", required = false) String sortOrder) {
        return Result.dataMessageHandler(
                () -> creditService.listTransactions(pageNum, pageSize, accountId, username, changeType, sortOrder),
                "获取流水失败");
    }
}
