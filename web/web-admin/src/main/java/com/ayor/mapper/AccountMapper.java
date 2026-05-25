package com.ayor.mapper;

import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AccountMapper extends BaseMapper<Account> {

    @Select("select * from account where username = #{username}")
    Account getAccountByName(String username);

    @Select("select username from account where account_id = #{accountId}")
    String getUsernameById(Integer accountId);

    @Select("select account_id from account where role_id = #{roleId}")
    List<Integer> getAccountIdsByRoleId(Integer roleId);
}
