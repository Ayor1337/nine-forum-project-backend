package com.ayor.mapper;

import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ThemeMapper extends BaseMapper<Theme> {

    @Select("select * from db_theme")
    List<Theme> getThemeList();

}
