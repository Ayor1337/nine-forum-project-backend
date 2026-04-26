package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 收藏管理服务接口(管理员版)
 *
 * 提供后台收藏管理功能,包括收藏记录的查询和删除等管理员专用操作。
 *
 * 主要功能:
 * - 收藏查询: 支持按帖子、用户等多种条件查询
 * - 收藏管理: 删除收藏记录
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Collect 收藏实体
 * @author ayor
 * @since 1.0.0
 */
public interface CollectService extends IService<Collect> {

    /**
     * 多条件查询收藏记录(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param threadId 帖子ID过滤条件;为null时不过滤
     * @param accountId 用户ID过滤条件;为null时不过滤
     * @return 分页结果,包含匹配条件的收藏记录
     */
    PageEntity<Collect> getCollects(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId);

    /**
     * 删除收藏记录(物理删除)
     * @param collectId 收藏记录ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteCollect(Integer collectId);
}
