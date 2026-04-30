package com.ayor.mapper;

import com.ayor.entity.pojo.AccountStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

public interface AccountStatMapper extends BaseMapper<AccountStat> {


    @Update("UPDATE account_stat " +
            "SET thread_count = " +
            "(SELECT COUNT(*) FROM thread WHERE account_id = account_stat.account_id)")
    void updateThreadCount();


    @Update("UPDATE account_stat " +
            "SET post_count = " +
            "(SELECT COUNT(*) FROM post WHERE account_id = account_stat.account_id)")
    void updatePostCount();

    @Update("UPDATE account_stat " +
            "SET following_count = " +
            "(SELECT COUNT(*) FROM user_relation " +
            "WHERE from_account_id = account_stat.account_id " +
            "AND relation_type = 'FOLLOW' " +
            "AND status = 'ACTIVE')")
    void updateFollowingCount();

    @Update("UPDATE account_stat " +
            "SET follower_count = " +
            "(SELECT COUNT(*) FROM user_relation " +
            "WHERE to_account_id = account_stat.account_id " +
            "AND relation_type = 'FOLLOW' " +
            "AND status = 'ACTIVE')")
    void updateFollowerCount();

    @Insert("INSERT INTO account_stat (account_id) VALUES (#{accountId})")
    boolean insertNewAccountStat(Integer accountId);



}
