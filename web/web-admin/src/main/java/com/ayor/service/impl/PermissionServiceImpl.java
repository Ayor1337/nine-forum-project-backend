package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.PermissionService;
import com.ayor.type.PermissionType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

    private final AccountMapper accountMapper;

    private final CacheManager cacheManager;

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
    public List<String> listPermissionOptions() {
        return PermissionType.options();
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
        if (!this.save(permission)) {
            return "创建权限失败";
        }
        evictUserInfoByRoleId(permission.getRoleId());
        return null;
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
        Integer originalRoleId = exist.getRoleId();
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
        if (!this.updateById(exist)) {
            return "更新权限失败";
        }
        evictUserInfoByRoleId(originalRoleId);
        evictUserInfoByRoleId(exist.getRoleId());
        return null;
    }

    @Override
    public String updatePermissions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "权限不能为空";
        }
        for (Permission permission : permissions) {
            String message = updatePermission(permission);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    /**
     * 删除指定权限记录。
     */
    @Override
    public String deletePermission(Integer permissionId) {
        if (permissionId == null) {
            return "权限不存在";
        }
        Permission exist = this.getById(permissionId);
        if (!this.removeById(permissionId)) {
            return "删除权限失败";
        }
        if (exist != null) {
            evictUserInfoByRoleId(exist.getRoleId());
        }
        return null;
    }

    @Override
    public String deletePermissions(List<Integer> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return "权限不能为空";
        }
        for (Integer permissionId : permissionIds) {
            String message = deletePermission(permissionId);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    private void evictUserInfoByRoleId(Integer roleId) {
        if (roleId == null) {
            return;
        }
        List<Integer> accountIds = accountMapper.getAccountIdsByRoleId(roleId);
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        Cache userInfoCache = cacheManager.getCache("userInfo");
        if (userInfoCache == null) {
            return;
        }
        for (Integer accountId : accountIds) {
            userInfoCache.evict(accountId);
        }
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
