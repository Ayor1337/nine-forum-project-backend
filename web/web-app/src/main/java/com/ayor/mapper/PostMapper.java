package com.ayor.mapper;

import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {

    @Select("select * from post where thread_id = #{threadId}")
    List<Post> getPostsByThreadId(Integer threadId);

    @Update("update post set is_deleted = 1 where thread_id = #{threadId}")
    Integer removePostsByThreadId(Integer threadId);

    @Select("select count(*) from post where account_id = #{accountId}")
    Integer getCountByAccountId(Integer accountId);

    @Select("""
            select p.*
            from post p
            inner join thread t on t.thread_id = p.thread_id
            where t.account_id = #{accountId}
              and p.account_id <> #{accountId}
              and (p.is_deleted = 0 or p.is_deleted is null)
              and (t.is_deleted = 0 or t.is_deleted is null)
            order by p.create_time desc
            """)
    Page<Post> listReplyMessages(Page<Post> page, @Param("accountId") Integer accountId);

}
