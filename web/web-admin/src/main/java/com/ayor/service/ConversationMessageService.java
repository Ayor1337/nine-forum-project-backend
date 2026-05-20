package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 私信消息管理服务接口(管理员版)
 *
 * 提供后台私信消息管理功能,包括消息查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 消息查询: 按对话分页查询消息
 * - 消息管理: 删除消息记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see ConversationMessage 私信消息实体
 * @author ayor
 * @since 1.0.0
 */
public interface ConversationMessageService extends IService<ConversationMessage> {

    /**
     * 获取对话的消息列表(分页)
     * @param conversationId 对话ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定对话的所有消息
     */
    PageEntity<ConversationMessageVO> getMessages(Integer conversationId, Integer pageNum, Integer pageSize);

    /**
     * 获取单条会话消息
     * @param messageId 消息ID
     * @return 会话消息
     */
    ConversationMessageVO getMessageById(Integer messageId);

    /**
     * 创建会话消息
     * @param message 会话消息
     * @return 操作结果消息
     */
    String createMessage(ConversationMessage message);

    /**
     * 更新会话消息
     * @param message 会话消息
     * @return 操作结果消息
     */
    String updateMessage(ConversationMessage message);

    /**
     * 删除消息记录(物理删除)
     * @param messageId 消息ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteMessage(Integer messageId);
}
