package com.ayor.service;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.entity.vo.PasskeyCredentialVO;
import com.ayor.entity.vo.PasskeyOptionsVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface PasskeyService {

    /**
     * 为当前账号生成注册 options。
     *
     * @param accountId 账号 ID
     * @return 包含 requestId 和 publicKey 的 options
     */
    PasskeyOptionsVO<Map<String, Object>> createRegistrationOptions(Integer accountId);

    /**
     * 完成 Passkey 注册。
     *
     * @param accountId 账号 ID
     * @param dto 注册完成请求
     * @return 失败消息，成功时返回 `null`
     */
    String registerCredential(Integer accountId, PasskeyRegistrationFinishDTO dto);

    /**
     * 列出当前账号绑定的 Passkey。
     *
     * @param accountId 账号 ID
     * @return Passkey 列表
     */
    List<PasskeyCredentialVO> listCredentials(Integer accountId);

    /**
     * 删除指定 Passkey。
     *
     * @param accountId 账号 ID
     * @param credentialId 凭证 ID
     * @return 失败消息，成功时返回 `null`
     */
    String deleteCredential(Integer accountId, Long credentialId);

    /**
     * 为无用户名 Passkey 登录生成 options。
     *
     * @return 包含 requestId 和 publicKey 的 options
     */
    PasskeyOptionsVO<Map<String, Object>> createAuthenticationOptions();

    /**
     * 完成 Passkey 登录并记录当前登录会话。
     *
     * @param dto 登录完成请求
     * @param request 当前 HTTP 请求
     * @return 登录成功后的授权信息，失败时返回 `null`
     */
    AuthorizeVO authenticate(PasskeyAuthenticationFinishDTO dto, HttpServletRequest request);
}
