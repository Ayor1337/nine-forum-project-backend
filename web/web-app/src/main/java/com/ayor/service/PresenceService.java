package com.ayor.service;

/**
 * 在线状态服务接口
 *
 * 管理用户的在线/离线状态，支持多设备多会话独立标记。
 *
 * 主要功能:
 * - 标记用户在线
 * - 标记用户离线
 * - 查询用户是否在线
 *
 * 技术特性:
 * - 基于 Redis 存储在线状态
 * - 支持多会话独立管理
 *
 * @author ayor
 * @since 1.0.0
 */
public interface PresenceService {

    void markOnline(Integer accountId, String sessionId);

    void markOffline(Integer accountId, String sessionId);

    boolean isOnline(Integer accountId);
}
