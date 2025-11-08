package com.ayor.mapper;

import com.ayor.entity.pojo.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT role_name FROM role WHERE role_id = #{roleId}")
    String getRoleNameById(Integer roleId);

    @Select("select role_name " +
            "from role " +
            "inner join account on role.role_id = account.role_id " +
            "where account.username = #{username}")
    String getRoleNameByUsername(String username);

    @Select("select topic_id " +
            "from role " +
            "inner join account on role.role_id = account.role_id " +
            "where account.username = #{username}")
    Integer getTopicIdByUsername(String username);




}
