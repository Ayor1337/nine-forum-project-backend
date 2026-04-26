package com.ayor.mapper;

import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {

    @Select("select * from account where username = #{username}")
    Account getAccountByName(String username);

    @Select("select username from account where account_id = #{accountId}")
    String getUsernameById(Integer accountId);
}
