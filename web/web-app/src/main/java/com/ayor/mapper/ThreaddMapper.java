package com.ayor.mapper;

import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ThreaddMapper extends BaseMapper<Threadd> {

    @Select("select * from db_thread where topic_id = #{topicId} order by create_time desc")
    List<Threadd> getThreadsByTopicId(Integer topicId);

    @Select("select title from db_thread where thread_id = #{threadId}")
    String getThreadTitleById(Integer threadId);

    List<Threadd> getThreadsByIds(List<Integer> threadIds);

    @Select("select topic_id from db_thread where thread_id = #{threadId}")
    Integer getTopicIdByThreadId(Integer threadId);

    @Update("update db_thread set tag_id = null where thread_id = #{threadId} and topic_id = #{topicId}")
    Boolean removeThreadTag(Integer threadId, Integer topicId);

    @Select("select * from db_thread where is_announcement = 1 and topic_id = #{topicId}")
    List<Threadd> getAnnouncementsByTopicId(Integer topicId);


    @Update("update db_thread set is_deleted = 1 where topic_id = #{topicId}")
    Integer deleteThreadByTopicId(Integer topicId);

    @Select("select count(*) from db_thread where account_id = #{accountId}")
    Integer getCountByAccountId(Integer accountId);


    @Update("UPDATE db_thread " +
            "SET post_count = " +
            "(SELECT COUNT(*) FROM db_post WHERE db_post.thread_id = db_thread.thread_id)")
    void updateThreadPostCount();


    @Update("UPDATE db_thread " +
            "SET like_count = " +
            "(SELECT COUNT(*) FROM db_like WHERE db_like.thread_id = db_thread.thread_id)")
    void updateLikeCount();

}
