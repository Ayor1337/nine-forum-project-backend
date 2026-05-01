package com.ayor.controller;

import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.dto.RegDTO;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AuthorizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthorizeController {

    private final AuthorizeService authorizeService;

    private final AccountService accountService;
    /**
     * 发送注册验证邮件。
     *
     * @param regDTO 注册验证请求，包含邮箱地址
     * @return 验证邮件发送结果
     */

    @PostMapping("/register-verifications")
    public Result<String> registerVerify(@RequestBody @Valid RegDTO regDTO) {
        return Result.dataMessageHandler(() -> authorizeService.createAuthorizeToken(regDTO.getEmail()), "邮件发送失败");
    }
    /**
     * 完成注册并创建账户。
     *
     * @param accountDTO 注册信息，包含用户名、密码和邮箱验证 token
     * @return 注册结果
     */

    @PostMapping("/registrations")
    public Result<Void> register(@RequestBody @Valid AccountDTO accountDTO) {
        return Result.messageHandler(() -> accountService.insertNewAccount(accountDTO));
    }
    /**
     * 校验注册邮箱的验证 token。
     *
     * @param email 目标邮箱
     * @param token 验证 token
     * @return 验证结果文本
     */

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
