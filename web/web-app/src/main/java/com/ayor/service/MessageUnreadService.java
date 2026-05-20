package com.ayor.service;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
import com.ayor.type.UnreadMessageType;

/**
 * 未读消息服务接口
 *
 * 提供用户未读消息的计数管理功能,支持多种消息类型的未读统计。
 *
 * 主要功能:
 * - 未读查询: 获取指定类型或全部类型的未读数量
 * - 未读清除: 清空或减少未读计数
 * - 未读增加: 增加未读计数
 *
 * 技术特性:
 * - 使用Redis存储未读计数,高性能
 * - 支持多种消息类型(回复、点赞、系统消息等)
 * - 集成WebSocket实时推送
 *
 * @see MessageUnread 未读消息对象
 * @see UnreadMessageType 未读消息类型枚举
 * @author ayor
 * @since 1.0.0
 */
public interface MessageUnreadService {

    /**
     * 获取指定类型的未读消息数量
     * @param userId 用户ID
     * @param type 消息类型枚举
     * @return 未读数量
     */
    Long getUnread(Integer userId, UnreadMessageType type);

    /**
     * 获取指定类型的未读消息数量(字符串类型)
     * @param userId 用户ID
     * @param type 消息类型字符串
     * @return 未读数量
     * @see #getUnread(Integer, UnreadMessageType) 推荐使用枚举版本
     */
    Long getUnread(Integer userId, String type);

    /**
     * 获取指定类型的未读消息对象
     * @param userId 用户ID
     * @param type 消息类型枚举
     * @return 未读消息对象,包含类型和数量
     */
    MessageUnread getUnreadVO(Integer userId, UnreadMessageType type);

    /**
     * 获取指定类型的未读消息对象(字符串类型)
     * @param userId 用户ID
     * @param type 消息类型字符串
     * @return 未读消息对象,包含类型和数量
     * @see #getUnreadVO(Integer, UnreadMessageType) 推荐使用枚举版本
     */
    MessageUnread getUnreadVO(Integer userId, String type);

    /**
     * 获取所有类型的未读消息汇总对象
     * @param userId 用户ID
     * @return 未读消息对象,包含所有类型的未读数量
     */
    MessageUnread getUnreadVO(Integer userId);

    /**
     * 获取所有类型的未读消息概览对象
     * @param userId 用户ID
     * @return 未读消息概览对象,包含总数和各类型未读数量
     */
    UnreadOverviewVO getUnreadOverviewVO(Integer userId);

    /**
     * 清除指定类型的未读消息(减少指定数量)
     * @param userId 用户ID
     * @param type 消息类型枚举
     * @param value 要减少的数量
     * @return 清除后的剩余未读数量
     */
    Long clearUnread(Integer userId, UnreadMessageType type, Long value);

    /**
     * 清空指定类型的所有未读消息
     * @param userId 用户ID
     * @param type 消息类型枚举
     * @return 清除后的未读数量(应为0)
     * @see #clearUnread(Integer, UnreadMessageType, Long) 清除指定数量
     */
    Long clearUnread(Integer userId, UnreadMessageType type);

    /**
     * 增加指定类型的未读消息数量
     * @param userId 用户ID
     * @param type 消息类型枚举
     * @param value 要增加的数量
     * @return 增加后的总未读数量
     */
    long addUnread(Integer userId, UnreadMessageType type, Long value);
}
