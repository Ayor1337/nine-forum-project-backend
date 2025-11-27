package com.ayor.service;

import com.ayor.entity.app.dto.ThemeDTO;
import com.ayor.entity.app.vo.ThemeTopicVO;
import com.ayor.entity.app.vo.ThemeVO;
import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface ThemeService extends IService<Theme> {
    List<ThemeVO> getThemeList();

    String insertTheme(ThemeDTO themeDTO);

    List<ThemeTopicVO> getThemeTopicList();
}
