package com.ayor.mapper;

import com.ayor.entity.pojo.TopicStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TopicStatMapper extends BaseMapper<TopicStat> {

    @Update("UPDATE topic_stat " +
            "SET thread_count = " +
            "(SELECT COUNT(*) FROM thread WHERE thread.topic_id = topic_stat.topic_id)")
    void updateThreadCount();

    @Update("UPDATE topic_stat " +
            "SET view_count = " +
            "(SELECT IFNULL(SUM(view_count), 0) FROM thread WHERE thread.topic_id = topic_stat.topic_id)")
    void updateViewCount();

    @Select("SELECT * FROM topic_stat WHERE topic_id = #{topicId}")
    TopicStat selectByTopicId(Integer topicId);

    @Insert("INSERT INTO topic_stat (topic_id) VALUES (#{topicId})")
    int initializeNewTopicStat(Integer topicId);


}
