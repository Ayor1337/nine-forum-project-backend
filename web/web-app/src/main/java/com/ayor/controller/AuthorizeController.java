package com.ayor.controller;

import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.dto.RegDTO;
import com.ayor.mail.EmailHtmlTemplates;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AuthorizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证授权")
@RequiredArgsConstructor
public class AuthorizeController {

    private final AuthorizeService authorizeService;

    private final AccountService accountService;

    private final EmailHtmlTemplates emailHtmlTemplates;
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
    @GetMapping(value = "/register-verifications", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String verify(@RequestParam("email") String email,
                         @RequestParam("token") String token) {
        if (authorizeService.validateAuthorizeToken(token, email)) {
            return emailHtmlTemplates.verifyResult(true, "邮箱验证成功", "你的邮箱已经完成验证，可以返回注册页面继续创建账号。");
        } else {
            return emailHtmlTemplates.verifyResult(false, "邮箱验证失败", "验证链接无效、已过期，或邮箱与链接不匹配。请重新获取验证邮件。");
        }

    }

}
