package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.MentionMessage;
import com.ayor.entity.vo.MentionMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @提及消息服务接口
 *
 * 处理帖子或评论中 @提及用户的消息通知，支持提及消息的创建和查询。
 *
 * 主要功能:
 * - 创建帖子 @提及消息
 * - 创建评论 @提及消息
 * - 查询 @提及消息列表
 *
 * @see MentionMessage 提及消息实体
 * @see MentionMessageVO 提及消息视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface MentionMessageService extends IService<MentionMessage> {

    void createThreadMentionMessages(String content, Integer fromAccountId, Integer threadId);

    void createPostMentionMessages(String content, Integer fromAccountId, Integer postId, Integer threadId);

    PageEntity<MentionMessageVO> listMentionMessages(Integer pageNum, Integer pageSize, Integer accountId);
}
