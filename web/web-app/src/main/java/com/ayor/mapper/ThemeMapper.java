package com.ayor.mapper;

import com.ayor.entity.pojo.Theme;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ThemeMapper extends BaseMapper<Theme> {

    @Select("select * from theme")
    List<Theme> getThemeList();

    @Select("select theme_id from topic where topic_id = #{TopicId}")
    Integer getThemeIdByTopicId(Integer TopicId);

}
