package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThemeDTO;
import com.ayor.entity.admin.vo.ThemeVO;
import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 主题管理服务接口(管理员版)
 *
 * 提供后台主题管理功能,包括主题的查询、创建、编辑和删除等管理员专用操作。
 * 主题是分类的上级概念,用于组织和分组多个相关分类。
 *
 * 主要功能:
 * - 主题查询: 分页查询所有主题
 * - 主题管理: 创建、编辑、删除主题
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Theme 主题实体
 * @see ThemeVO 主题视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface ThemeService extends IService<Theme> {

    /**
     * 获取所有主题列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含所有主题
     */
    PageEntity<ThemeVO> getThemes(Integer pageNum, Integer pageSize);

    /**
     * 创建新主题
     * @param themeDTO 主题数据传输对象,包含主题名称、描述等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createTheme(ThemeDTO themeDTO);

    /**
     * 更新主题信息
     * @param themeDTO 主题数据传输对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateTheme(ThemeDTO themeDTO);

    /**
     * 删除主题(逻辑删除)
     * @param themeId 主题ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 删除主题前需确保该主题下没有分类
     */
    String deleteTheme(Integer themeId);
}
