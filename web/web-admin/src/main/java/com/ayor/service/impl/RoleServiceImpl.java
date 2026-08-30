package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.RoleDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.AccountVO;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.entity.vo.RoleVO;
import com.ayor.entity.pojo.Role;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.RoleService;
import com.ayor.type.PermissionType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private static final int DEFAULT_USER_ROLE_ID = 3;

    private final TopicMapper topicMapper;

    private final AccountMapper accountMapper;

    private final PermissionMapper permissionMapper;

    /**
     * 查询全部角色，并补充其绑定的话题名称，供管理端展示使用。
     */
    @Override
    public List<RoleVO> getRoles() {
        List<Role> roles = this.lambdaQuery().list();
        List<RoleVO> roleVos = new ArrayList<>();
        roles.forEach(role -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(role, roleVO);
            roleVO.setTopicName(topicMapper.getTopicNameById(role.getTopicId()));
            roleVos.add(roleVO);
        });
        return roleVos;
    }

    @Override
    public RoleVO getRoleById(Integer roleId) {
        if (roleId == null) {
            return null;
        }
        Role role = this.getById(roleId);
        if (role == null) {
            return null;
        }
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        roleVO.setTopicName(topicMapper.getTopicNameById(role.getTopicId()));
        return roleVO;
    }

    /**
     * 创建新角色，并保存其关联的话题配置。
     */
    @Override
    public String createRole(RoleDTO roleDTO) {
        if (roleDTO == null || !StringUtils.hasText(roleDTO.getRoleName())) {
            return "角色名称不能为空";
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        return this.save(role) ? null : "创建角色失败";
    }

    /**
     * 更新角色信息，保持角色主键不变。
     */
    @Override
    public String updateRole(RoleDTO roleDTO) {
        if (roleDTO == null || roleDTO.getRoleId() == null) {
            return "角色不存在";
        }
        Role role = this.getById(roleDTO.getRoleId());
        if (role == null) {
            return "角色不存在";
        }
        BeanUtils.copyProperties(roleDTO, role);
        return this.updateById(role) ? null : "更新角色失败";
    }

    /**
     * 删除指定角色。
     */
    @Override
    public String deleteRole(Integer roleId) {
        if (roleId == null) {
            return "角色不存在";
        }
        if (this.getById(roleId) == null) {
            return "角色不存在";
        }
        Long accountCount = accountMapper.selectCount(Wrappers.<Account>lambdaQuery()
                .eq(Account::getRoleId, roleId));
        if (accountCount != null && accountCount > 0) {
            return "该角色下仍有用户，不能删除";
        }
        Long permissionCount = permissionMapper.selectCount(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getRoleId, roleId));
        if (permissionCount != null && permissionCount > 0) {
            return "该角色下仍有权限，不能删除";
        }
        return this.removeById(roleId) ? null : "删除角色失败";
    }

    @Override
    public PageEntity<AccountVO> listRoleAccounts(Integer roleId, Integer pageNum, Integer pageSize) {
        if (roleId == null || this.getById(roleId) == null) {
            return new PageEntity<>(0L, List.of());
        }
        Page<Account> page = accountMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Account>lambdaQuery().eq(Account::getRoleId, roleId));
        return new PageEntity<>(page.getTotal(), toAccountVOList(page.getRecords()));
    }

    @Override
    public String addAccountToRole(Integer roleId, Integer accountId) {
        if (roleId == null || this.getById(roleId) == null) {
            return "角色不存在";
        }
        Account account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        account.setRoleId(roleId);
        return accountMapper.updateById(account) > 0 ? null : "添加用户到角色失败";
    }

    @Override
    public String removeAccountFromRole(Integer roleId, Integer accountId) {
        if (roleId == null || this.getById(roleId) == null) {
            return "角色不存在";
        }
        Account account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        if (!roleId.equals(account.getRoleId())) {
            return "用户不属于该角色";
        }
        account.setRoleId(DEFAULT_USER_ROLE_ID);
        return accountMapper.updateById(account) > 0 ? null : "从角色移除用户失败";
    }

    @Override
    public List<PermissionVO> listRolePermissions(Integer roleId) {
        if (roleId == null || this.getById(roleId) == null) {
            return List.of();
        }
        List<Permission> permissions = permissionMapper.selectList(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getRoleId, roleId));
        return toPermissionVOList(permissions);
    }

    @Override
    public String addPermissionToRole(Integer roleId, String permission) {
        if (roleId == null || this.getById(roleId) == null) {
            return "角色不存在";
        }
        if (!PermissionType.isKnown(permission)) {
            return "权限标识不存在";
        }
        Long count = permissionMapper.selectCount(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getRoleId, roleId)
                .eq(Permission::getPermission, permission));
        if (count != null && count > 0) {
            return null;
        }
        Permission rolePermission = new Permission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermission(permission);
        return permissionMapper.insert(rolePermission) > 0 ? null : "添加角色权限失败";
    }

    @Override
    public String removePermissionFromRole(Integer roleId, String permission) {
        if (roleId == null || this.getById(roleId) == null) {
            return "角色不存在";
        }
        if (!PermissionType.isKnown(permission)) {
            return "权限标识不存在";
        }
        int affected = permissionMapper.delete(Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getRoleId, roleId)
                .eq(Permission::getPermission, permission));
        return affected > 0 ? null : "角色权限不存在";
    }

    private List<AccountVO> toAccountVOList(List<Account> accounts) {
        List<AccountVO> accountVOS = new ArrayList<>();
        for (Account account : accounts) {
            AccountVO accountVO = new AccountVO();
            BeanUtils.copyProperties(account, accountVO);
            accountVOS.add(accountVO);
        }
        return accountVOS;
    }

    private List<PermissionVO> toPermissionVOList(List<Permission> permissions) {
        List<PermissionVO> permissionVOS = new ArrayList<>();
        for (Permission permission : permissions) {
            PermissionVO permissionVO = new PermissionVO();
            BeanUtils.copyProperties(permission, permissionVO);
            permissionVOS.add(permissionVO);
        }
        return permissionVOS;
    }

}
