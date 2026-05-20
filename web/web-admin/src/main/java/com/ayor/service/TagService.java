package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.vo.TagVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 标签管理服务接口(管理员版)
 *
 * 提供后台标签管理功能,包括标签的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 标签查询: 按分类查询、分页查询
 * - 标签管理: 创建、编辑、删除标签
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Tag 标签实体
 * @author ayor
 * @since 1.0.0
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取指定分类下的所有标签(不分页)
     * @param topicId 分类ID
     * @return 标签列表
     */
    List<TagVO> listTags(Integer topicId);

    /**
     * 获取单个标签详情
     * @param tagId 标签ID
     * @return 标签详情
     */
    TagVO getTagById(Integer tagId);

    /**
     * 创建新标签
     * @param tag 标签实体对象,包含标签名称、所属分类等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createTag(Tag tag);

    /**
     * 更新标签信息
     * @param tag 标签实体对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateTag(Tag tag);

    /**
     * 删除标签(逻辑删除)
     * @param tagId 标签ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteTag(Integer tagId);

    /**
     * 分页查询指定分类下的标签
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param topicId 分类ID
     * @return 分页结果,包含指定分类下的标签列表
     */
    PageEntity<TagVO> pageTags(Integer pageNum, Integer pageSize, Integer topicId);
}
