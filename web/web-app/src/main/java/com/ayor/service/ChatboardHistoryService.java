package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.entity.pojo.ChatboardHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 聊天室历史记录服务接口
 *
 * 提供分类聊天室的消息历史记录管理功能。
 *
 * 主要功能:
 * - 消息发送: 发送聊天室消息并存储历史
 * - 历史查询: 获取聊天室历史消息
 *
 * @see ChatboardHistory 聊天室历史实体
 * @see ChatboardHistoryVO 聊天室历史视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface ChatboardHistoryService extends IService<ChatboardHistory> {

    /**
     * 发送聊天室消息
     * @param accountId 发送者用户ID
     * @param topicId 分类ID,标识聊天室
     * @param content 消息内容
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String insertChatboardHistory(Integer accountId,
                                  Integer topicId,
                                  String content);

    /**
     * 获取聊天室历史消息
     *
     * @param topicId  分类ID,标识聊天室
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 历史消息视图对象列表
     */
    PageEntity<ChatboardHistoryVO> getChatboardHistory(Integer topicId, Integer pageNum, Integer pageSize);
}
