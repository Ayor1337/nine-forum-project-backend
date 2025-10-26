package com.ayor.mapper;

import com.ayor.entity.pojo.TopicStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TopicStatMapper extends BaseMapper<TopicStat> {

    @Update("UPDATE db_topic_stat " +
            "SET thread_count = " +
            "(SELECT COUNT(*) FROM db_thread WHERE db_thread.topic_id = db_topic_stat.topic_id)")
    void updateThreadCount();

    @Update("UPDATE db_topic_stat " +
            "SET view_count = " +
            "(SELECT IFNULL(SUM(view_count), 0) FROM db_thread WHERE db_thread.topic_id = db_topic_stat.topic_id)")
    void updateViewCount();

    @Select("SELECT * FROM db_topic_stat WHERE topic_id = #{topicId}")
    TopicStat selectByTopicId(Integer topicId);

    @Insert("INSERT INTO db_topic_stat (topic_id) VALUES (#{topicId})")
    int initializeNewTopicStat(Integer topicId);


}
