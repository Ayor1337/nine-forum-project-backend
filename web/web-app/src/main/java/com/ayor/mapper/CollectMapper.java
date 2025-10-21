package com.ayor.mapper;

import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface CollectMapper extends BaseMapper<Collect> {

    @Select("select count(*) from db_collect where thread_id = #{threadId}")
    Integer getCollectCountByThreadId(Integer threadId);

}
