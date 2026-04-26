package com.ayor.mapper;

import com.ayor.entity.pojo.LikeThread;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface LikeThreadMapper extends BaseMapper<LikeThread> {

    @Select("SELECT COUNT(*) FROM like_thread WHERE thread_id = #{threadId}")
    Integer getLikeCountByThreadId(Integer threadId);

}
