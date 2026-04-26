package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 分类聊天管理服务接口(管理员版)
 *
 * 提供后台分类聊天记录管理功能,包括聊天查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 聊天查询: 按分类分页查询聊天记录
 * - 聊天管理: 删除聊天记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see TopicChat 分类聊天实体
 * @author ayor
 * @since 1.0.0
 */
public interface TopicChatService extends IService<TopicChat> {

    /**
     * 获取分类聊天记录(分页)
     * @param topicId 分类ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定分类的聊天记录
     */
    PageEntity<TopicChat> getTopicChats(Integer topicId, Integer pageNum, Integer pageSize);

    /**
     * 删除聊天记录(物理删除)
     * @param topicChatId 聊天记录ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteTopicChat(Integer topicChatId);
}
