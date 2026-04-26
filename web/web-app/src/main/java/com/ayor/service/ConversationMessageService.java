package com.ayor.service;

import com.ayor.aspect.chat.ChatNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.type.NotificationType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 私信消息服务接口
 *
 * 提供私信消息的发送和查询功能。
 *
 * 主要功能:
 * - 消息发送: 发送私信消息并触发实时通知
 * - 消息查询: 分页获取对话消息列表
 *
 * @see ConversationMessage 私信消息实体
 * @see ConversationMessageVO 私信消息视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface ConversationMessageService extends IService<ConversationMessage> {

    /**
     * 发送私信消息
     * @param conversationMessage 消息数据传输对象,包含消息内容、对话ID等
     * @param accountId 发送者用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 发送成功后会通过WebSocket推送实时通知给接收者
     */
    String sendMessage(ConversationMessageDTO conversationMessage, Integer accountId);

    /**
     * 获取对话的消息列表(分页)
     * @param conversationId 对话ID
     * @param accountId 当前用户ID,用于权限验证
     * @param pageNum 页码,从1开始
     * @return 分页结果,包含消息视图对象列表
     */
    PageEntity<ConversationMessageVO> getConversationMessageList(Integer conversationId, Integer accountId, Integer pageNum);
}
