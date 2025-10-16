package com.ayor.mapper;

import com.ayor.entity.app.vo.UserPermissionVO;
import com.ayor.entity.pojo.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT permission FROM db_permission " +
            "INNER JOIN db_role " +
            "on db_permission.role_id = db_role.role_id " +
            "INNER JOIN " +
            "nine_forum.db_account da on db_role.role_id = " +
            "da.role_id WHERE da.username = #{username}")
    List<String> getPermissionsByUsername(String username);


    UserPermissionVO getUserPermissionVO(String username);

}
