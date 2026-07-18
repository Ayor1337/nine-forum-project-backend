package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.FollowMessage;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.vo.FollowMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 关注消息服务接口
 *
 * 处理用户关注帖子后产生的消息通知，支持关注消息的创建和查询。
 *
 * 主要功能:
 * - 创建帖子关注消息
 * - 查询关注消息列表
 *
 * @see FollowMessage 关注消息实体
 * @see FollowMessageVO 关注消息视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface FollowMessageService extends IService<FollowMessage> {

    void createThreadFollowMessages(Threadd thread);

    PageEntity<FollowMessageVO> listFollowMessages(Integer pageNum, Integer pageSize, Integer accountId);
}
