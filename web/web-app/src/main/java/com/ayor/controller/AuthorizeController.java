package com.ayor.controller;

import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.dto.RegDTO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AuthorizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证授权")
@RequiredArgsConstructor
public class AuthorizeController {

    private final AuthorizeService authorizeService;

    private final AccountService accountService;
    /**
     * 发送注册验证邮件。
     */
    @Operation(summary = "发送注册验证邮件")
    @PostMapping("/register-verifications")
    public Result<String> registerVerify(@RequestBody @Valid RegDTO regDTO) {
        return Result.dataMessageHandler(() -> authorizeService.createAuthorizeToken(regDTO.getEmail()), "邮件发送失败");
    }
    /**
     * 完成注册并创建账户。
     */
    @Operation(summary = "完成注册")
    @PostMapping("/registrations")
    public Result<Void> register(@RequestBody @Valid AccountDTO accountDTO) {
        return Result.messageHandler(() -> accountService.insertNewAccount(accountDTO));
    }
    /**
     * 校验注册邮箱的验证 token。
     */
    @Operation(summary = "校验注册邮箱验证码")
    @GetMapping("/register-verifications")
    @ResponseBody
    public String verify(@RequestParam("email") String email,
                         @RequestParam("token") String token) {
        if (authorizeService.validateAuthorizeToken(token, email)) {
            return "验证成功";
        } else {
            return "验证失败";
        }

    }

}
