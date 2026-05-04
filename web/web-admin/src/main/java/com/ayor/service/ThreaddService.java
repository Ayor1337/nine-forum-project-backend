package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.ThreadTableVO;
import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 帖子管理服务接口(管理员版)
 *
 * 提供后台帖子管理功能,包括帖子的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 帖子查询: 分页查询所有帖子
 * - 帖子管理: 创建、编辑、删除帖子
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Threadd 帖子实体
 * @see ThreadTableVO 帖子表格视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface ThreaddService extends IService<Threadd> {

    /**
     * 获取所有帖子列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含帖子表格视图对象列表
     */
    PageEntity<ThreadTableVO> getThreads(Integer pageNum, Integer pageSize);

    /**
     * 获取单个帖子详情
     * @param threadId 帖子ID
     * @return 帖子详情
     */
    Threadd getThreadById(Integer threadId);

    /**
     * 创建新帖子(管理员操作)
     * @param threadDTO 帖子数据传输对象,包含标题、内容、分类等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createThread(ThreadDTO threadDTO);

    /**
     * 更新帖子内容(管理员操作)
     * @param threadDTO 帖子数据传输对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateThread(ThreadDTO threadDTO);

    /**
     * 删除帖子(管理员操作,物理删除)
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteThread(Integer threadId);
}
