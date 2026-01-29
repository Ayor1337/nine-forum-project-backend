package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.document.ThreadDoc;
import com.ayor.entity.app.dto.TagUpdateDTO;
import com.ayor.entity.app.dto.ThreadDTO;
import com.ayor.entity.app.vo.AnnouncementVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 帖子(Thread)业务服务接口
 *
 * 负责论坛帖子的核心业务逻辑,包括帖子的完整生命周期管理。
 *
 * 主要功能:
 * - 帖子查询: 按分类、用户、ID等多种条件查询
 * - 帖子管理: 创建、编辑、删除(逻辑删除)
 * - 公告管理: 设置/取消公告
 * - 标签管理: 添加、修改、移除标签
 * - 统计更新: 浏览量、回复数、点赞数等
 * - 数据转换: Entity到VO/Doc的转换
 *
 * 注意事项:
 * - 所有删除操作均为逻辑删除(软删除)
 * - 浏览量更新使用悲观锁防止并发问题
 *
 * @see Threadd 帖子实体
 * @see ThreadVO 帖子视图对象
 * @see ThreadDoc Elasticsearch文档对象
 * @author ayor
 * @since 1.0.0
 */
public interface ThreaddService extends IService<Threadd> {

    /**
     * 按分类ID获取帖子列表(不分页)
     * @param topicId 分类ID
     * @return 帖子视图对象列表
     */
    List<ThreadVO> getThreadVOsByTopicId(Integer topicId);

    /**
     * 按分类ID获取帖子列表(分页版本)
     * @param topicId 分类ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含ThreadVO列表和总记录数
     */
    PageEntity<ThreadVO> getThreadVOsByTopicId(Integer topicId, Integer pageNum, Integer pageSize);

    /**
     * 获取帖子标题
     * @param threadId 帖子ID
     * @return 帖子标题字符串
     */
    String getThreadTitleById(Integer threadId);

    /**
     * 获取帖子详细信息
     * @param threadId 帖子ID
     * @return 帖子视图对象,包含完整的帖子信息、作者信息等
     */
    ThreadVO getThreadById(Integer threadId);

    /**
     * 按用户ID获取帖子列表(分页)
     * @param accountId 用户账户ID
     * @param currentPage 当前页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含该用户发布的所有帖子
     */
    PageEntity<ThreadVO> getThreadPagesByUserId(Integer accountId, Integer currentPage, Integer pageSize);

    /**
     * 删除帖子(用户操作,逻辑删除)
     *
     * 用户只能删除自己发布的帖子。
     *
     * @param threadId 帖子ID
     * @param accountId 操作用户ID,用于权限验证
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeThreadById(Integer threadId, Integer accountId);

    /**
     * 永久删除帖子(管理员操作)
     *
     * 此操作会物理删除数据库记录,无法恢复。
     *
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String permRemoveThreadById(Integer threadId);

    /**
     * 设置帖子为公告
     * @param threadId 帖子ID
     * @param topicId 分类ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String setAnnouncementByThreadId(Integer threadId, Integer topicId);

    /**
     * 取消帖子的公告状态
     * @param threadId 帖子ID
     * @param topicId 分类ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeAnnouncementByThreadId(Integer threadId, Integer topicId);

    /**
     * 获取指定分类的公告帖子列表
     * @param topicId 分类ID
     * @return 公告视图对象列表
     */
    List<AnnouncementVO> getAnnouncementThreads(Integer topicId);

    /**
     * 创建新帖子
     * @param threadDTO 帖子数据传输对象,包含标题、内容、分类等信息
     * @param accountId 发帖用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String insertThread(ThreadDTO threadDTO, Integer accountId);

    /**
     * 更新帖子标签
     * @param tagUpdateDTO 标签更新数据传输对象,包含标签ID列表
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateThreadTag(TagUpdateDTO tagUpdateDTO);

    /**
     * 移除帖子标签
     * @param threadId 帖子ID
     * @param topicId 分类ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removeThreadTag(Integer threadId, Integer topicId);

    /**
     * 更新帖子统计数据(定时任务调用)
     *
     * 此方法由调度任务定期调用,用于同步更新帖子的回复数、点赞数等统计信息。
     *
     * @note 此方法由定时任务触发,不应手动调用
     */
    void updateThreadStat();

    /**
     * 更新帖子浏览量(增加1)
     *
     * 使用悲观锁(SELECT FOR UPDATE)防止并发更新导致的计数不准确问题。
     *
     * @param threadId 帖子ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 使用悲观锁机制,高并发场景下可能影响性能
     */
    String updateViewCount(Integer threadId);

    /**
     * 将帖子实体列表转换为Elasticsearch文档列表
     *
     * 用于将数据库中的帖子数据同步到Elasticsearch搜索引擎。
     *
     * @param threads 帖子实体列表
     * @return Elasticsearch文档对象列表
     * @see ThreadDoc Elasticsearch文档结构定义
     */
    List<ThreadDoc> toThreadDocs(List<Threadd> threads);
}
