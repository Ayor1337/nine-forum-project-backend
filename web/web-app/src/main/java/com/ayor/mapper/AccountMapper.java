package com.ayor.mapper;

import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {


    @Select("select * from account where username = #{username}")
    Account getAccountByUsername(String username);

    @Select("select * from account where account_id = #{id}")
    Account getAccountById(Integer id);

    @Select("select username from account where account_id = #{id}")
    String getUsernameById(Integer id);

    @Select("select nickname from account where account_id = #{id}")
    String getNicknameById(Integer id);

    @Select("select account_id from account where username = #{username}")
    Integer getAccountIdByUsername(String username);

    @Select("select avatar_url from account where account_id = #{accountId}")
    String getAvatarUrlById(Integer accountId);







}
