package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.PermissionService;
import com.ayor.type.PermissionType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RoleMapper roleMapper;

    /**
     * 查询角色下的权限列表；未指定角色时返回全部权限。
     */
    @Override
    public List<PermissionVO> listPermissions(Integer roleId) {
        return toVOList(this.lambdaQuery()
                .eq(roleId != null, Permission::getRoleId, roleId)
        .list());
    }

    @Override
    public PermissionVO getPermissionById(Integer permissionId) {
        if (permissionId == null) {
            return null;
        }
        return toVO(this.getById(permissionId));
    }

    /**
     * 创建一条角色权限绑定记录。
     */
    @Override
    public String createPermission(Permission permission) {
        if (permission == null || permission.getRoleId() == null) {
            return "角色不能为空";
        }
        if (roleMapper.selectById(permission.getRoleId()) == null) {
            return "角色不存在";
        }
        if (!StringUtils.hasText(permission.getPermission())) {
            return "权限标识不能为空";
        }
        if (!PermissionType.isKnown(permission.getPermission())) {
            return "权限标识不存在";
        }
        return this.save(permission) ? null : "创建权限失败";
    }

    /**
     * 更新权限记录中非空的字段。
     */
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
            if (roleMapper.selectById(permission.getRoleId()) == null) {
                return "角色不存在";
            }
            exist.setRoleId(permission.getRoleId());
        }
        if (StringUtils.hasText(permission.getPermission())) {
            if (!PermissionType.isKnown(permission.getPermission())) {
                return "权限标识不存在";
            }
            exist.setPermission(permission.getPermission());
        }
        return this.updateById(exist) ? null : "更新权限失败";
    }

    /**
     * 删除指定权限记录。
     */
    @Override
    public String deletePermission(Integer permissionId) {
        if (permissionId == null) {
            return "权限不存在";
        }
        return this.removeById(permissionId) ? null : "删除权限失败";
    }

    private List<PermissionVO> toVOList(List<Permission> permissions) {
        List<PermissionVO> permissionVOS = new ArrayList<>();
        for (Permission permission : permissions) {
            permissionVOS.add(toVO(permission));
        }
        return permissionVOS;
    }

    private PermissionVO toVO(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionVO permissionVO = new PermissionVO();
        BeanUtils.copyProperties(permission, permissionVO);
        return permissionVO;
    }
}
