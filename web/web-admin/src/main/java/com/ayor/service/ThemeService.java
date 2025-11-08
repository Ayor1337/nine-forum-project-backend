package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThemeDTO;
import com.ayor.entity.admin.vo.ThemeVO;
import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.extension.service.IService;


public interface ThemeService extends IService<Theme> {
    PageEntity<ThemeVO> getThemes(Integer pageNum, Integer pageSize);

    String createTheme(ThemeDTO themeDTO);

    String updateTheme(ThemeDTO themeDTO);

    String deleteTheme(Integer themeId);
}
