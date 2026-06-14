package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.vo.AccountVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import jakarta.validation.Valid;
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

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "后台用户管理", description = "后台用户列表、详情、状态和违规处理接口")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 分页查询后台用户，支持按关键字、状态和角色过滤。
     */
    @Operation(summary = "分页查询后台用户，支持按关键字、状态和角色过滤")
    @GetMapping
    public Result<PageEntity<AccountVO>> getAccounts(@Parameter(description = "搜索关键字") @RequestParam(value = "query", required = false) String query,
                                                     @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                     @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                     @Parameter(description = "状态") @RequestParam(value = "status", required = false) Integer status,
                                                     @Parameter(description = "角色ID") @RequestParam(value = "role_id", required = false) Integer roleId) {
        if (roleId != null) {
            return Result.dataMessageHandler(() -> accountService.getAccountsByRoleId(pageNum, pageSize, roleId), "获取用户失败");
        }
        return Result.dataMessageHandler(() -> accountService.getAccounts(query, pageNum, pageSize, status), "获取用户列表失败");
    }

    /**
     * 读取单个用户的详细信息。
     */
    @Operation(summary = "读取单个用户的详细信息")
    @GetMapping("/{accountId}")
    public Result<AccountVO> getAccountById(@Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId) {
        return Result.dataMessageHandler(() -> accountService.getAccountById(accountId), "获取用户失败");
    }

    /**
     * 获取用户下拉选项，供表单联动选择使用。
     */
    @Operation(summary = "获取用户下拉选项，供表单联动选择使用")
    @GetMapping("/options")
    public Result<List<AccountVO>> listUsers(@Parameter(description = "搜索关键字") @RequestParam(name = "query", required = false) String query) {
        return Result.dataMessageHandler(() -> accountService.getAccountsAsSelectOptions(query), "获取用户列表失败");
    }

    /**
     * 对指定用户执行违规处理。
     */
    @Operation(summary = "对指定用户执行违规处理")
    @PostMapping("/{accountId}/violations")
    public Result<Void> violationProfile(@Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId,
                                         @Parameter(description = "违规处理类型") @RequestParam(name = "type") String type) {
        return Result.messageHandler(() -> accountService.violationProfile(accountId, type));
    }

    /**
     * 更新指定用户的后台资料。
     */
    @Operation(summary = "更新指定用户的后台资料")
    @PutMapping("/{accountId}")
    public Result<Void> updateAccount(@Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId,
                                      @Parameter(description = "用户资料") @RequestBody @Valid AccountDTO accountDTO) {
        accountDTO.setAccountId(accountId);
        return Result.messageHandler(() -> accountService.updateAccount(accountDTO));
    }

    @Operation(summary = "执行后台管理操作")
    @PutMapping("/{accountId}/restore")
    public Result<Void> restoreAccount(@Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> accountService.restoreAccount(accountId));
    }

    /**
     * 删除指定用户。
     */
    @Operation(summary = "删除指定用户")
    @DeleteMapping("/{accountId}")
    public Result<Void> deleteAccount(@Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> accountService.deleteAccount(accountId));
    }
}
