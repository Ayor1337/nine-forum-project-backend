package com.ayor.service;

import com.ayor.entity.app.dto.TagDTO;
import com.ayor.entity.app.vo.TagVO;
import com.ayor.entity.pojo.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 标签服务接口(用户端)
 *
 * 提供帖子标签的基本管理功能。
 *
 * 主要功能:
 * - 标签查询: 获取所有标签、按分类获取标签
 * - 标签管理: 创建新标签
 *
 * @see Tag 标签实体
 * @see TagVO 标签视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有标签列表(不分页)
     * @return 标签视图对象列表
     */
    List<TagVO> listTags();

    /**
     * 获取指定分类下的所有标签
     * @param topicId 分类ID
     * @return 标签视图对象列表
     */
    List<TagVO> listTagsByTopicId(Integer topicId);

    /**
     * 创建新标签
     * @param tagDTO 标签数据传输对象,包含标签名称、所属分类等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String insertNewTag(TagDTO tagDTO);
}
