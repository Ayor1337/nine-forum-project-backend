package com.ayor.mapper;

import com.ayor.entity.pojo.AccountStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

public interface AccountStatMapper extends BaseMapper<AccountStat> {


    @Update("UPDATE db_account_stat " +
            "SET thread_count = " +
            "(SELECT COUNT(*) FROM db_thread WHERE account_id = db_account_stat.account_id)")
    void updateThreadCount();


    @Update("UPDATE db_account_stat " +
            "SET post_count = " +
            "(SELECT COUNT(*) FROM db_post WHERE account_id = db_account_stat.account_id)")
    void updatePostCount();



}
