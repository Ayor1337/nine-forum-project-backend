package com.ayor.service;

import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类服务接口(用户端)
 *
 * 提供论坛分类的基本管理功能,包括分类查询、创建、编辑和删除。
 *
 * 主要功能:
 * - 分类查询: 按ID获取名称、按主题获取列表
 * - 分类管理: 创建、编辑、删除分类
 *
 * @see Topic 分类实体
 * @see TopicVO 分类视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface TopicService extends IService<Topic> {

    /**
     * 获取分类名称
     * @param topicId 分类ID
     * @return 分类名称字符串
     */
    String getTopicNameById(Integer topicId);

    /**
     * 获取指定主题下的所有分类
     * @param themeId 主题ID
     * @return 分类视图对象列表
     */
    List<TopicVO> getTopicListByThemeId(Integer themeId);

    /**
     * 创建新分类
     * @param topicDTO 分类数据传输对象,包含分类名称、描述等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String insertTopic(TopicDTO topicDTO);

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
     */
    String deleteTopic(Integer topicId);
}
