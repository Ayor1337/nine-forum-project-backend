package com.ayor.mapper;

import com.ayor.entity.pojo.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface TopicMapper extends BaseMapper<Topic> {

    @Select("select title from topic where topic_id = #{topicId}")
    String getTopicNameById(Integer topicId);

    @Select("select count(*) > 0 from topic where topic_id = #{topicId} and is_deleted = 0")
    boolean existsById(Integer topicId);

}
