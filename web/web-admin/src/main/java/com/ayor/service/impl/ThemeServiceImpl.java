package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThemeDTO;
import com.ayor.entity.admin.vo.ThemeVO;
import com.ayor.entity.pojo.Theme;
import com.ayor.mapper.ThemeMapper;
import com.ayor.service.ThemeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ThemeServiceImpl extends ServiceImpl<ThemeMapper, Theme> implements ThemeService {


    @Override
    public PageEntity<ThemeVO> getThemes(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            return null;
        }
        Page<Theme> page = this.lambdaQuery().page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public String createTheme(ThemeDTO themeDTO) {
        if (themeDTO == null || !StringUtils.hasText(themeDTO.getTitle())) {
            return "主题名称不能为空";
        }
        Theme theme = new Theme();
        BeanUtils.copyProperties(themeDTO, theme);
        theme.setIsDeleted(false);
        return this.save(theme) ? null : "创建主题失败";
    }

    @Override
    public String updateTheme(ThemeDTO themeDTO) {
        if (themeDTO == null || themeDTO.getThemeId() == null) {
            return "主题不存在";
        }
        Theme theme = this.getById(themeDTO.getThemeId());
        if (theme == null) {
            return "主题不存在";
        }
        BeanUtils.copyProperties(themeDTO, theme);
        return this.updateById(theme) ? null : "更新主题失败";
    }

    @Override
    public String deleteTheme(Integer themeId) {
        if (themeId == null) {
            return "主题不存在";
        }
        Theme theme = this.getById(themeId);
        if (theme == null) {
            return "主题不存在";
        }
        theme.setIsDeleted(true);
        return this.updateById(theme) ? null : "删除主题失败";
    }

    private List<ThemeVO> toVOList (List<Theme> themeList) {
        List<ThemeVO> themeVOList = new ArrayList<>();
        for (Theme theme : themeList) {
            ThemeVO themeVO = new ThemeVO();
            BeanUtils.copyProperties(theme, themeVO);
            themeVOList.add(themeVO);
        }
        return themeVOList;
    }

}
