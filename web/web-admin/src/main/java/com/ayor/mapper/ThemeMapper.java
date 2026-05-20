package com.ayor.mapper;

import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ThemeMapper extends BaseMapper<Theme> {

    @Select("select * from theme")
    List<Theme> getThemeList();

    @Select("select count(*) > 0 from theme where theme_id = #{themeId} and is_deleted = 0")
    boolean existsById(Integer themeId);

}
