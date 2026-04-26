package com.ayor.service;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.type.UnreadMessageType;

/**
 * 广播服务接口
 *
 * 提供系统消息和违规通知的广播推送功能,用于向用户推送实时消息。
 *
 * 主要功能:
 * - 系统消息广播: 推送系统通知消息
 * - 违规通知广播: 推送用户违规处理通知
 *
 * 技术特性:
 * - 集成RabbitMQ消息队列
 * - 通过WebSocket推送实时通知
 *
 * @see UserSystemMessage 系统消息对象
 * @see UserViolationMessage 违规通知消息对象
 * @author ayor
 * @since 1.0.0
 */
public interface BroadcastService {

    /**
     * 广播系统消息
     * @param message 系统消息对象,支持泛型消息内容
     * @param <T> 消息内容类型
     * @note 消息会通过RabbitMQ发送并通过WebSocket推送给目标用户
     */
    <T> void userSystemBroadcast(UserSystemMessage<T> message);

    /**
     * 广播用户违规通知
     * @param message 违规通知消息对象,支持泛型消息内容
     * @param <T> 消息内容类型
     * @note 消息会通过RabbitMQ发送并通过WebSocket推送给违规用户
     */
    <T> void userViolationBroadcast(UserViolationMessage<T> message);
}
