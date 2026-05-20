package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 聊天室历史管理服务接口(管理员版)
 *
 * 提供后台聊天室历史记录管理功能,包括历史查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 历史查询: 按分类分页查询聊天室历史
 * - 历史管理: 删除历史记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see ChatboardHistory 聊天室历史实体
 * @author ayor
 * @since 1.0.0
 */
public interface ChatboardHistoryService extends IService<ChatboardHistory> {

    /**
     * 获取聊天室历史记录(分页)
     * @param topicId 分类ID,标识聊天室
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定聊天室的历史记录
     */
    PageEntity<ChatboardHistoryVO> getHistories(Integer topicId, Integer pageNum, Integer pageSize);

    /**
     * 获取单条聊天板历史记录
     * @param historyId 历史记录ID
     * @return 聊天板历史记录
     */
    ChatboardHistoryVO getHistoryById(Integer historyId);

    /**
     * 创建聊天板历史记录
     * @param history 聊天板历史记录
     * @return 操作结果消息
     */
    String createHistory(ChatboardHistory history);

    /**
     * 更新聊天板历史记录
     * @param history 聊天板历史记录
     * @return 操作结果消息
     */
    String updateHistory(ChatboardHistory history);

    /**
     * 删除历史记录(物理删除)
     * @param historyId 历史记录ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteHistory(Integer historyId);
}
