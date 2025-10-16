package com.ayor.mapper;

import com.ayor.entity.pojo.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT role_name FROM db_role WHERE role_id = #{roleId}")
    String getRoleNameById(Integer roleId);

    @Select("select role_name " +
            "from db_role " +
            "inner join db_account on db_role.role_id = db_account.role_id " +
            "where db_account.username = #{username}")
    String getRoleNameByUsername(String username);

    @Select("select topic_id " +
            "from db_role " +
            "inner join db_account on db_role.role_id = db_account.role_id " +
            "where db_account.username = #{username}")
    Integer getTopicIdByUsername(String username);




}
