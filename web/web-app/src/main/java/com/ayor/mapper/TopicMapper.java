package com.ayor.mapper;

import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TopicMapper extends BaseMapper<Topic> {

    @Select("select * from topic")
    List<Topic> getTopicList();

    @Select("select * from topic where theme_id = #{themeId}")
    List<Topic> getTopicByThemeId(Integer themeId);

    @Select("select is_deleted from topic where topic_id = #{topicId}")
    Boolean isTopicDelete(Integer topicId);

    @Select("select count(*) from topic where theme_id = #{themeId}")
    Integer getCountByThemeId(Integer themeId);



}
