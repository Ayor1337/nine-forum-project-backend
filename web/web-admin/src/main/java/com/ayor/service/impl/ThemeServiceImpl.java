package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
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
