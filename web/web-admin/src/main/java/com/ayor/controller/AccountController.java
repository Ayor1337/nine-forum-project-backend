package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
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

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public Result<PageEntity<AccountVO>> getAccounts(@RequestParam(value = "query", required = false) String query,
                                                     @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                     @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                     @RequestParam(value = "status", required = false) Integer status,
                                                     @RequestParam(value = "role_id", required = false) Integer roleId) {
        if (roleId != null) {
            return Result.dataMessageHandler(() -> accountService.getAccountsByRoleId(pageNum, pageSize, roleId), "获取用户失败");
        }
        return Result.dataMessageHandler(() -> accountService.getAccounts(query, pageNum, pageSize, status), "获取用户列表失败");
    }

    @GetMapping("/{accountId}")
    public Result<AccountVO> getAccountById(@PathVariable("accountId") Integer accountId) {
        return Result.dataMessageHandler(() -> accountService.getAccountById(accountId), "获取用户失败");
    }

    @GetMapping("/options")
    public Result<List<AccountVO>> listUsers(@RequestParam(name = "query", required = false) String query) {
        return Result.dataMessageHandler(() -> accountService.getAccountsAsSelectOptions(query), "获取用户列表失败");
    }

    @PostMapping("/{accountId}/violations")
    public Result<Void> violationProfile(@PathVariable("accountId") Integer accountId,
                                         @RequestParam(name = "type") String type) {
        return Result.messageHandler(() -> accountService.violationProfile(accountId, type));
    }

    @PutMapping("/{accountId}")
    public Result<Void> updateAccount(@PathVariable("accountId") Integer accountId,
                                      @RequestBody @Valid AccountDTO accountDTO) {
        accountDTO.setAccountId(accountId);
        return Result.messageHandler(() -> accountService.updateAccount(accountDTO));
    }

    @DeleteMapping("/{accountId}")
    public Result<Void> deleteAccount(@PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> accountService.deleteAccount(accountId));
    }
}
