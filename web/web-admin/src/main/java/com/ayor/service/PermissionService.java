package com.ayor.service;

import com.ayor.entity.pojo.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    List<Permission> listPermissions(Integer roleId);

    String createPermission(Permission permission);

    String updatePermission(Permission permission);

    String deletePermission(Integer permissionId);
}
