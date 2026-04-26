package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 收藏服务接口
 *
 * 提供帖子收藏功能,包括收藏、取消收藏、收藏状态查询和收藏列表管理。
 *
 * 主要功能:
 * - 收藏操作: 收藏、取消收藏帖子
 * - 状态查询: 检查收藏状态、获取收藏数量
 * - 收藏列表: 获取用户的收藏帖子列表
 *
 * @see Collect 收藏实体
 * @see ThreadVO 帖子视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface CollectService extends IService<Collect> {

    /**
     * 收藏帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述(如已收藏)
     */
    String insertCollect(Integer accountId, Integer threadId);

    /**
     * 取消收藏帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeCollect(Integer accountId, Integer threadId);

    /**
     * 检查用户是否已收藏指定帖子
     * @param accountId 用户ID
     * @param threadId 帖子ID
     * @return true=已收藏,false=未收藏
     */
    Boolean isCollectedByAccountId(Integer accountId, Integer threadId);

    /**
     * 获取帖子的收藏数量
     * @param threadId 帖子ID
     * @return 收藏数量
     */
    Integer getCollectCountByThreadId(Integer threadId);

    /**
     * 获取用户的收藏帖子列表(分页)
     * @param accountId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户收藏的帖子视图对象列表
     */
    PageEntity<ThreadVO> getCollectsByAccountId(Integer accountId, Integer pageNum, Integer pageSize);
}
