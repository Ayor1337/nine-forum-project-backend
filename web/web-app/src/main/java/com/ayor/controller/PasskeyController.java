package com.ayor.controller;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.entity.vo.PasskeyCredentialVO;
import com.ayor.entity.vo.PasskeyOptionsVO;
import com.ayor.result.Result;
import com.ayor.service.PasskeyService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passkeys")
@RequiredArgsConstructor
/**
 * Passkey 相关接口。
 */
public class PasskeyController {

    private final PasskeyService passkeyService;

    private final SecurityUtils securityUtils;

    /**
     * 生成当前账号的 Passkey 注册参数。
     *
     * @return 注册 options
     */
    @PostMapping("/registration/options")
    public Result<PasskeyOptionsVO<Map<String, Object>>> createRegistrationOptions() {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> passkeyService.createRegistrationOptions(accountId), "生成 Passkey 注册参数失败");
    }

    /**
     * 完成当前账号的 Passkey 注册。
     *
     * @param dto 注册完成请求体
     * @return 结果
     */
    @PostMapping("/registrations")
    public Result<Void> registerCredential(@RequestBody @Valid PasskeyRegistrationFinishDTO dto) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> passkeyService.registerCredential(accountId, dto));
    }

    /**
     * 列出当前账号已绑定的 Passkey。
     *
     * @return Passkey 列表
     */
    @GetMapping
    public Result<List<PasskeyCredentialVO>> listCredentials() {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> passkeyService.listCredentials(accountId), "获取 Passkey 列表失败");
    }

    /**
     * 删除当前账号绑定的指定 Passkey。
     *
     * @param credentialId 凭证 ID
     * @return 结果
     */
    @DeleteMapping("/{credential_id}")
    public Result<Void> deleteCredential(@PathVariable("credential_id") Long credentialId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> passkeyService.deleteCredential(accountId, credentialId));
    }

    /**
     * 生成无用户名 Passkey 登录参数。
     *
     * @return 登录 options
     */
    @PostMapping("/authentication/options")
    public Result<PasskeyOptionsVO<Map<String, Object>>> createAuthenticationOptions() {
        return Result.dataMessageHandler(passkeyService::createAuthenticationOptions, "生成 Passkey 登录参数失败");
    }

    /**
     * 完成 Passkey 登录。
     *
     * @param dto 登录完成请求体
     * @return 授权结果
     */
    @PostMapping("/authentications")
    public Result<AuthorizeVO> authenticate(@RequestBody @Valid PasskeyAuthenticationFinishDTO dto) {
        return Result.dataMessageHandler(() -> passkeyService.authenticate(dto), "Passkey 登录失败");
    }
}
