package com.ayor.mapper;

import com.ayor.entity.app.vo.UserPermissionVO;
import com.ayor.entity.pojo.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT permission FROM permission " +
            "INNER JOIN role " +
            "on permission.role_id = role.role_id " +
            "INNER JOIN " +
            "nine_forum.account da on role.role_id = " +
            "da.role_id WHERE da.account_id = #{accountId}")
    List<String> getPermissionsByAccountId(Integer accountId);


    UserPermissionVO getUserPermissionVO(Integer accountId);

}
