package com.ayor.service;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.PermissionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 权限管理服务接口(管理员版)
 *
 * 提供后台权限管理功能,包括权限的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 权限查询: 按角色查询权限列表
 * - 权限管理: 创建、编辑、删除权限
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Permission 权限实体
 * @author ayor
 * @since 1.0.0
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 获取指定角色的所有权限
     * @param roleId 角色ID
     * @return 权限实体列表
     */
    List<PermissionVO> listPermissions(Integer roleId);

    /**
     * 获取单条权限记录
     * @param permissionId 权限ID
     * @return 权限记录
     */
    PermissionVO getPermissionById(Integer permissionId);

    /**
     * 创建新权限
     * @param permission 权限实体对象,包含权限名称、资源路径等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createPermission(Permission permission);

    /**
     * 更新权限信息
     * @param permission 权限实体对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updatePermission(Permission permission);

    /**
     * 批量更新权限信息
     * @param permissions 权限实体列表
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updatePermissions(List<Permission> permissions);

    /**
     * 删除权限(物理删除)
     * @param permissionId 权限ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deletePermission(Integer permissionId);

    /**
     * 批量删除权限(物理删除)
     * @param permissionIds 权限ID列表
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deletePermissions(List<Integer> permissionIds);
}
