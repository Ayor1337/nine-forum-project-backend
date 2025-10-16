package com.ayor.mapper;

import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {


    @Select("select * from db_account where username = #{username}")
    Account getAccountByUsername(String username);

    @Select("select * from db_account where account_id = #{id}")
    Account getAccountById(Integer id);

    @Select("select nickname from db_account where account_id = #{id}")
    String getNicknameById(Integer id);

    @Select("select account_id from db_account where username = #{username}")
    Integer getAccountIdByUsername(String username);







}
