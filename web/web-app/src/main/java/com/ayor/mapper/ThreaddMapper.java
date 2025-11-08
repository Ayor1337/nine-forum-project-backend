package com.ayor.mapper;

import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ThreaddMapper extends BaseMapper<Threadd> {

    @Select("select * from thread where topic_id = #{topicId} order by create_time desc")
    List<Threadd> getThreadsByTopicId(Integer topicId);

    @Select("select title from thread where thread_id = #{threadId}")
    String getThreadTitleById(Integer threadId);

    List<Threadd> getThreadsByIds(List<Integer> threadIds);

    @Select("select topic_id from thread where thread_id = #{threadId}")
    Integer getTopicIdByThreadId(Integer threadId);

    @Update("update thread set tag_id = null where thread_id = #{threadId} and topic_id = #{topicId}")
    Boolean removeThreadTag(Integer threadId, Integer topicId);

    @Select("select * from thread where is_announcement = 1 and topic_id = #{topicId}")
    List<Threadd> getAnnouncementsByTopicId(Integer topicId);


    @Update("update thread set is_deleted = 1 where topic_id = #{topicId}")
    Integer deleteThreadByTopicId(Integer topicId);

    @Select("select count(*) from thread where account_id = #{accountId}")
    Integer getCountByAccountId(Integer accountId);


    @Update("UPDATE thread " +
            "SET post_count = " +
            "(SELECT COUNT(*) FROM post WHERE post.thread_id = thread.thread_id)")
    void updateThreadPostCount();


    @Update("UPDATE thread " +
            "SET like_count = " +
            "(SELECT COUNT(*) FROM like_thread WHERE like_thread.thread_id = thread.thread_id)")
    void updateLikeCount();

}
