package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类管理服务接口(管理员版)
 *
 * 提供后台分类管理功能,包括分类的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 分类查询: 全部查询、按主题查询、搜索查询
 * - 分类管理: 创建、编辑、删除分类
 * - 下拉选项: 提供分类选择器数据源
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Topic 分类实体
 * @see TopicVO 分类视图对象
 * @see TopicDTO 分类数据传输对象
 * @author ayor
 * @since 1.0.0
 */
public interface TopicService extends IService<Topic> {

    /**
     * 获取所有分类列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含所有分类
     */
    PageEntity<TopicVO> getTopics(Integer pageNum, Integer pageSize);

    /**
     * 按主题ID获取分类列表(分页)
     * @param themeId 主题ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含指定主题下的所有分类
     */
    PageEntity<TopicVO> getTopicsByThemeId(Integer themeId, Integer pageNum, Integer pageSize);

    /**
     * 获取单个分类详情
     * @param topicId 分类ID
     * @return 分类详情
     */
    TopicVO getTopicById(Integer topicId);

    /**
     * 按关键词搜索分类作为下拉选项
     * @param query 搜索关键词,支持分类名称模糊匹配
     * @return 匹配的分类视图对象列表,用于下拉选择器
     */
    List<TopicVO> getTopicsAsOptions(String query);

    /**
     * 创建新分类
     * @param topicDTO 分类数据传输对象,包含分类名称、描述、所属主题等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createTopic(TopicDTO topicDTO);

    /**
     * 更新分类信息
     * @param topicDTO 分类数据传输对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateTopic(TopicDTO topicDTO);

    /**
     * 删除分类(逻辑删除)
     * @param topicId 分类ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 删除分类前需确保该分类下没有帖子
     */
    String deleteTopic(Integer topicId);
}
