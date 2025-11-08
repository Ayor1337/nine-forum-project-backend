package com.ayor.mapper;

import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

}
