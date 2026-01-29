package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 评论管理服务接口(管理员版)
 *
 * 提供后台评论管理功能,包括评论的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 评论查询: 按帖子、用户、ID等多种条件查询
 * - 评论管理: 创建、编辑、删除评论
 * - 批量操作: 支持分页查询便于批量管理
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Post 评论实体
 * @author ayor
 * @since 1.0.0
 */
public interface PostService extends IService<Post> {

    /**
     * 按帖子ID获取评论列表(分页)
     * @param threadId 帖子ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定帖子的所有评论
     */
    PageEntity<Post> getPostsByThreadId(Integer threadId, Integer pageNum, Integer pageSize);

    /**
     * 按用户ID获取评论列表(分页)
     * @param accountId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定用户发布的所有评论
     */
    PageEntity<Post> getPostsByAccountId(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 获取评论详细信息
     * @param postId 评论ID
     * @return 评论实体对象,包含完整的评论数据
     */
    Post getPostById(Integer postId);

    /**
     * 创建新评论(管理员操作)
     * @param post 评论实体对象,包含评论内容、所属帖子等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createPost(Post post);

    /**
     * 更新评论内容(管理员操作)
     * @param post 评论实体对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updatePost(Post post);

    /**
     * 删除评论(管理员操作,物理删除)
     * @param postId 评论ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deletePost(Integer postId);
}
