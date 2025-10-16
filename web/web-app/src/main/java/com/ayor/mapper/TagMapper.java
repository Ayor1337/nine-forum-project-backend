package com.ayor.mapper;

import com.ayor.entity.pojo.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    @Select("select * from db_tag")
    List<Tag> getTagList();

    @Select("select * from db_tag where topic_id = #{themeId}")
    List<Tag> getTagByTopicId(Integer themeId);

    @Select("select * from db_tag where tag_id = #{tagId}")
    Tag getTagById(Integer tagId);

}
