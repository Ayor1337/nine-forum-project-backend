package com.ayor.mapper;

import com.ayor.entity.pojo.Like;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface LikeMapper extends BaseMapper<Like> {

    @Select("SELECT COUNT(*) FROM db_like WHERE thread_id = #{threadId}")
    Integer getLikeCountByThreadId(Integer threadId);

}
