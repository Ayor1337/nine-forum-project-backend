package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 对话管理服务接口(管理员版)
 *
 * 提供后台私信对话管理功能,包括对话查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 对话查询: 支持按用户过滤的分页查询
 * - 对话管理: 删除对话记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Conversation 对话实体
 * @author ayor
 * @since 1.0.0
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 多条件查询对话记录(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param alphaAccountId 参与者A的用户ID过滤条件;为null时不过滤
     * @param betaAccountId 参与者B的用户ID过滤条件;为null时不过滤
     * @return 分页结果,包含匹配条件的对话记录
     */
    PageEntity<Conversation> getConversations(Integer pageNum, Integer pageSize, Integer alphaAccountId, Integer betaAccountId);

    /**
     * 删除对话记录(物理删除)
     * @param conversationId 对话ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteConversation(Integer conversationId);
}
