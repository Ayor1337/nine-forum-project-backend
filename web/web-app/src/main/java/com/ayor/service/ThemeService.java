package com.ayor.service;

import com.ayor.entity.dto.ThemeDTO;
import com.ayor.entity.vo.ThemeTopicVO;
import com.ayor.entity.vo.ThemeVO;
import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 主题服务接口(用户端)
 *
 * 提供论坛主题的基本管理功能,主题是分类的上级概念。
 *
 * 主要功能:
 * - 主题查询: 获取主题列表、主题-分类树形结构
 * - 主题管理: 创建新主题
 *
 * @see Theme 主题实体
 * @see ThemeVO 主题视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface ThemeService extends IService<Theme> {

    /**
     * 获取所有主题列表(不分页)
     * @return 主题视图对象列表
     */
    List<ThemeVO> getThemeList();

    /**
     * 创建新主题
     * @param themeDTO 主题数据传输对象,包含主题名称、描述等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String insertTheme(ThemeDTO themeDTO);

    /**
     * 获取主题-分类树形结构列表
     * @return 主题-分类视图对象列表,包含每个主题及其下属分类
     */
    List<ThemeTopicVO> getThemeTopicList();
}
