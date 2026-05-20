package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.entity.pojo.LikeThread;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 帖子点赞服务接口
 *
 * 提供帖子点赞功能,包括点赞、取消点赞、点赞状态查询和点赞列表管理。
 *
 * 主要功能:
 * - 点赞操作: 点赞、取消点赞帖子
 * - 状态查询: 检查点赞状态、获取点赞数量
 * - 点赞列表: 获取用户的点赞帖子列表
 *
 * @see LikeThread 点赞实体
 * @see ThreadVO 帖子视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface LikeThreadService extends IService<LikeThread> {

    /**
     * 点赞帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述(如已点赞)
     */
    String insertLikeThreadId(Integer accountId, Integer threadId);

    /**
     * 取消点赞帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeLikeThreadId(Integer accountId, Integer threadId);

    /**
     * 获取用户的点赞帖子列表(分页)
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 用户ID
     * @param currentPage 当前页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户点赞的帖子视图对象列表
     */
    PageEntity<ThreadVO> getLikesByAccountId(Integer viewerId, Integer accountId, Integer currentPage, Integer pageSize);

    /**
     * 获取帖子的点赞数量
     * @param threadId 帖子ID
     * @return 点赞数量
     */
    Integer getLikeCountByThreadId(Integer threadId);

    /**
     * 检查用户是否已点赞指定帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return true=已点赞,false=未点赞
     */
    Boolean isLikedByAccountId(Integer accountId, Integer threadId);
}
