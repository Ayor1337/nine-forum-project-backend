package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.type.UserViolationType;
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
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/list")
    public Result<PageEntity<AccountVO>> getAccounts(@RequestParam(value = "query", required = false) String query,
                                          @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                          @RequestParam(value = "status", required = false) Integer status) {
        return Result.dataMessageHandler(() ->  accountService.getAccounts(query, pageNum, pageSize, status), "获取用户列表失败");
    }

    @GetMapping("/get_account_by_id")
    public Result<AccountVO> getAccountById(@RequestParam("account_id") Integer accountId) {
        return Result.dataMessageHandler(() -> accountService.getAccountById(accountId), "获取用户失败");
    }

    @GetMapping("/get_account_by_role_id")
    public Result<PageEntity<AccountVO>> getAccountByRoleId(@RequestParam("page_num") Integer pageNum,
                                                            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                            @RequestParam("role_id") Integer roleId) {
        return Result.dataMessageHandler(() -> accountService.getAccountsByRoleId(pageNum, pageSize, roleId), "获取用户失败");
    }

    @GetMapping("/list_user_options")
    public Result<List<AccountVO>> listUsers(@RequestParam(name = "query", required = false) String query) {
        return Result.dataMessageHandler(() -> accountService.getAccountsAsSelectOptions(query), "获取用户列表失败");
    }

    @PostMapping("/submit_violation")
    public Result<Void> violationProfile(@RequestParam(name = "accountId") Integer accountId,
                                         @RequestParam(name = "type") String type) {
        return Result.messageHandler(() -> accountService.violationProfile(accountId, type));
    }


    @PutMapping("/update")
    public Result<Void> updateAccount(@RequestBody @Valid AccountDTO accountDTO) {
        return Result.messageHandler(() -> accountService.updateAccount(accountDTO));
    }

    @DeleteMapping("/{account_id}")
    public Result<Void> deleteAccount(@PathVariable("account_id") Integer accountId) {
        return Result.messageHandler(() -> accountService.deleteAccount(accountId));
    }

}
