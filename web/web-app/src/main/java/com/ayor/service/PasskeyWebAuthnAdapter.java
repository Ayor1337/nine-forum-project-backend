package com.ayor.service;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.pojo.PasskeyCredential;

public interface PasskeyWebAuthnAdapter {

    /**
     * 使用 WebAuthn4J 校验注册结果并组装持久化实体。
     *
     * @param accountId 账号 ID
     * @param dto 注册完成请求
     * @param snapshot challenge 快照
     * @return 可直接入库的凭证实体
     */
    PasskeyCredential verifyRegistration(Integer accountId,
                                         PasskeyRegistrationFinishDTO dto,
                                         PasskeyRequestStore.ChallengeSnapshot snapshot);

    /**
     * 使用 WebAuthn4J 校验登录结果并返回新的签名计数。
     *
     * @param dto 登录完成请求
     * @param snapshot challenge 快照
     * @param credential 已持久化的凭证
     * @return 校验后的签名计数
     */
    Long verifyAuthentication(PasskeyAuthenticationFinishDTO dto,
                              PasskeyRequestStore.ChallengeSnapshot snapshot,
                              PasskeyCredential credential);
}
