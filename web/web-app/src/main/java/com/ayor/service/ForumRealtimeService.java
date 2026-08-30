package com.ayor.service;

import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;

/**
 * 论坛实时消息推送服务接口
 *
 * 通过 WebSocket 向论坛前端推送帖子创建、评论创建等实时事件。
 *
 * 主要功能:
 * - 推送帖子创建事件
 * - 推送评论创建事件
 *
 * 技术特性:
 * - 基于 STOMP 协议的实时推送
 * - 事务提交后推送，避免脏数据
 *
 * @see Threadd 帖子实体
 * @see Post 评论实体
 * @author ayor
 * @since 1.0.0
 */
public interface ForumRealtimeService {

    void publishThreadCreated(Threadd thread);

    void publishPostCreated(Post post);
}
