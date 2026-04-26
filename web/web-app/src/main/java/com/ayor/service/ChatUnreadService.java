package com.ayor.service;

/**
 * 私信未读服务接口
 *
 * 提供私信对话的未读消息计数管理功能。
 *
 * 主要功能:
 * - 未读查询: 获取对话未读消息数量
 * - 未读清除: 清空对话未读计数
 * - 未读增加: 增加对话未读计数
 *
 * 技术特性:
 * - 使用Redis存储未读计数
 *
 * @author ayor
 * @since 1.0.0
 */
public interface ChatUnreadService {

    /**
     * 获取对话的未读消息数量
     * @param conversationId 对话ID
     * @param fromUserId 对方用户ID
     * @return 未读消息数量
     */
    Long getUnread(Integer conversationId, Integer fromUserId);

    /**
     * 清空对话的未读消息计数
     * @param conversationId 对话ID
     * @param fromUserId 对方用户ID
     * @return 清空后的未读数量(应为0)
     */
    long clearUnread(Integer conversationId, Integer fromUserId);

    /**
     * 增加对话的未读消息计数
     * @param conversationId 对话ID
     * @param fromUserId 对方用户ID
     * @return 增加后的总未读数量
     */
    long addUnread(Integer conversationId, Integer fromUserId);
}
