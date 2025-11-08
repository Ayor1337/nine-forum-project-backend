package com.ayor.mapper;

import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface TopicMapper extends BaseMapper<Topic> {

    @Select("select title from topic where topic_id = #{topicId}")
    String getTopicNameById(Integer topicId);


}
