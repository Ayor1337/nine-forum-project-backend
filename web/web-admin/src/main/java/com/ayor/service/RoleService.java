package com.ayor.service;

import com.ayor.entity.dto.RoleDTO;
import com.ayor.entity.vo.RoleVO;
import com.ayor.entity.pojo.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色管理服务接口(管理员版)
 *
 * 提供后台角色管理功能,包括角色的查询、创建、编辑和删除等管理员专用操作。
 *
 * 主要功能:
 * - 角色查询: 获取所有角色列表
 * - 角色管理: 创建、编辑、删除角色
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Role 角色实体
 * @see RoleVO 角色视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface RoleService extends IService<Role> {

    /**
     * 获取所有角色列表(不分页)
     * @return 角色视图对象列表,包含角色信息和权限配置
     */
    List<RoleVO> getRoles();

    /**
     * 创建新角色
     * @param roleDTO 角色数据传输对象,包含角色名称、权限等信息
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String createRole(RoleDTO roleDTO);

    /**
     * 更新角色信息
     * @param roleDTO 角色数据传输对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateRole(RoleDTO roleDTO);

    /**
     * 删除角色(逻辑删除)
     * @param roleId 角色ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 删除角色前需确保没有用户使用该角色
     */
    String deleteRole(Integer roleId);
}
