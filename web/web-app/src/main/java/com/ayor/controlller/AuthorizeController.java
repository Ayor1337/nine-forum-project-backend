package com.ayor.controlller;

import com.ayor.entity.app.dto.AccountDTO;
import com.ayor.entity.app.dto.RegDTO;
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

    @PostMapping("/register_verify")
    public Result<String> registerVerify(@RequestBody @Valid RegDTO regDTO) {
        return Result.dataMessageHandler(() -> authorizeService.createAuthorizeToken(regDTO.getEmail()), "邮件发送失败");
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid AccountDTO accountDTO) {
        return Result.messageHandler(() -> accountService.insertNewAccount(accountDTO));
    }

    @GetMapping("/verify")
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
