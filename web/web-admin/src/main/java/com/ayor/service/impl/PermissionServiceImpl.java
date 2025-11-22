package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.mapper.PermissionMapper;
import com.ayor.service.PermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public List<Permission> listPermissions(Integer roleId) {
        return this.lambdaQuery()
                .eq(roleId != null, Permission::getRoleId, roleId)
                .list();
    }

    @Override
    public String createPermission(Permission permission) {
        if (permission == null || permission.getRoleId() == null) {
            return "角色不能为空";
        }
        if (!StringUtils.hasText(permission.getPermission())) {
            return "权限标识不能为空";
        }
        return this.save(permission) ? null : "创建权限失败";
    }

    @Override
    public String updatePermission(Permission permission) {
        if (permission == null || permission.getPermissionId() == null) {
            return "权限不存在";
        }
        Permission exist = this.getById(permission.getPermissionId());
        if (exist == null) {
            return "权限不存在";
        }
        if (permission.getRoleId() != null) {
            exist.setRoleId(permission.getRoleId());
        }
        if (StringUtils.hasText(permission.getPermission())) {
            exist.setPermission(permission.getPermission());
        }
        return this.updateById(exist) ? null : "更新权限失败";
    }

    @Override
    public String deletePermission(Integer permissionId) {
        if (permissionId == null) {
            return "权限不存在";
        }
        return this.removeById(permissionId) ? null : "删除权限失败";
    }
}
