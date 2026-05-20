package com.ayor.service;

import java.time.Instant;
import java.util.List;

public interface PasskeyRequestStore {

    /**
     * 请求类型。
     */
    enum RequestType {
        REGISTRATION,
        AUTHENTICATION
    }

    /**
     * Redis 中保存的 challenge 快照。
     *
     * @param requestId 请求 ID
     * @param type 请求类型
     * @param challenge challenge 值
     * @param rpId 依赖方 ID
     * @param origins 允许的 origin 列表
     * @param accountId 关联账号，登录请求时为空
     * @param userHandle 账号句柄，登录请求时为空
     * @param expiresAt 过期时间
     */
    record ChallengeSnapshot(
            String requestId,
            RequestType type,
            String challenge,
            String rpId,
            List<String> origins,
            Integer accountId,
            String userHandle,
            Instant expiresAt
    ) {
    }

    /**
     * 保存 challenge 快照。
     *
     * @param snapshot 快照
     */
    void save(ChallengeSnapshot snapshot);

    /**
     * 按请求 ID 读取快照。
     *
     * @param requestId 请求 ID
     * @return 快照，不存在或过期时返回 `null`
     */
    ChallengeSnapshot load(String requestId);

    /**
     * 删除指定请求 ID 的快照。
     *
     * @param requestId 请求 ID
     */
    void remove(String requestId);
}
