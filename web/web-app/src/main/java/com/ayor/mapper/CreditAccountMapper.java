package com.ayor.mapper;

import com.ayor.entity.pojo.CreditAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CreditAccountMapper extends BaseMapper<CreditAccount> {

    @Insert("""
            INSERT INTO credit_account (account_id, balance)
            VALUES (#{accountId}, 0)
            ON DUPLICATE KEY UPDATE account_id = account_id
            """)
    void initAccount(@Param("accountId") Integer accountId);

    @Select("""
            SELECT *
            FROM credit_account
            WHERE account_id = #{accountId}
            FOR UPDATE
            """)
    CreditAccount selectForUpdate(@Param("accountId") Integer accountId);

    @Update("""
            UPDATE credit_account
            SET balance = balance + #{amount}, update_time = NOW()
            WHERE account_id = #{accountId}
            """)
    int updateBalance(@Param("accountId") Integer accountId, @Param("amount") Long amount);
}
