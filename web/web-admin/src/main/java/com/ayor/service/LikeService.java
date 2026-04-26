package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 点赞管理服务接口(管理员版)
 *
 * 提供后台点赞管理功能,包括点赞记录的查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 点赞查询: 支持按帖子、用户等多种条件查询
 * - 点赞管理: 删除点赞记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see LikeThread 点赞实体
 * @author ayor
 * @since 1.0.0
 */
public interface LikeService extends IService<LikeThread> {

    /**
     * 多条件查询点赞记录(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param threadId 帖子ID过滤条件;为null时不过滤
     * @param accountId 用户ID过滤条件;为null时不过滤
     * @return 分页结果,包含匹配条件的点赞记录
     */
    PageEntity<LikeThread> getLikes(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId);

    /**
     * 删除点赞记录(物理删除)
     * @param likeId 点赞记录ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteLike(Integer likeId);
}
