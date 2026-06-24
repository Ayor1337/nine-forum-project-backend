package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Permission;
import com.ayor.entity.pojo.Role;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.TopicMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PermissionMapper permissionMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(topicMapper, accountMapper, permissionMapper);
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    // 测试删除角色拒绝角色带有账号
    @Test
    void deleteRoleRejectsRoleWithAccounts() {
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(accountMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        String message = roleService.deleteRole(2);

        assertThat(message).isEqualTo("该角色下仍有用户，不能删除");
        verify(roleMapper, never()).deleteById(2);
    }

    // 测试删除角色拒绝角色带有权限
    @Test
    void deleteRoleRejectsRoleWithPermissions() {
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(accountMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        String message = roleService.deleteRole(2);

        assertThat(message).isEqualTo("该角色下仍有权限，不能删除");
        verify(roleMapper, never()).deleteById(2);
    }

    // 测试新增账号到角色分配角色 ID
    @Test
    void addAccountToRoleAssignsRoleId() {
        Account account = account(7, 3);
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(accountMapper.selectById(7)).thenReturn(account);
        when(accountMapper.updateById(any(Account.class))).thenReturn(1);

        String message = roleService.addAccountToRole(2, 7);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(accountCaptor.capture());
        assertThat(message).isNull();
        assertThat(accountCaptor.getValue().getRoleId()).isEqualTo(2);
    }

    // 测试移除账号从角色重置到默认用户角色
    @Test
    void removeAccountFromRoleResetsToDefaultUserRole() {
        Account account = account(7, 2);
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(accountMapper.selectById(7)).thenReturn(account);
        when(accountMapper.updateById(any(Account.class))).thenReturn(1);

        String message = roleService.removeAccountFromRole(2, 7);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(accountCaptor.capture());
        assertThat(message).isNull();
        assertThat(accountCaptor.getValue().getRoleId()).isEqualTo(3);
    }

    // 测试移除账号从角色拒绝账号不属于角色
    @Test
    void removeAccountFromRoleRejectsAccountOutsideRole() {
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(accountMapper.selectById(7)).thenReturn(account(7, 4));

        String message = roleService.removeAccountFromRole(2, 7);

        assertThat(message).isEqualTo("用户不属于该角色");
        verify(accountMapper, never()).updateById(any(Account.class));
    }

    // 测试新增权限到角色拒绝未知权限
    @Test
    void addPermissionToRoleRejectsUnknownPermission() {
        when(roleMapper.selectById(2)).thenReturn(role(2));

        String message = roleService.addPermissionToRole(2, "UNKNOWN");

        assertThat(message).isEqualTo("权限标识不存在");
        verify(permissionMapper, never()).insert(any(Permission.class));
    }

    // 测试新增权限到角色创建已知权限
    @Test
    void addPermissionToRoleCreatesKnownPermission() {
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(permissionMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionMapper.insert(any(Permission.class))).thenReturn(1);

        String message = roleService.addPermissionToRole(2, "DELETE_THREAD");

        ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionMapper).insert(permissionCaptor.capture());
        assertThat(message).isNull();
        assertThat(permissionCaptor.getValue().getRoleId()).isEqualTo(2);
        assertThat(permissionCaptor.getValue().getPermission()).isEqualTo("DELETE_THREAD");
    }

    // 测试列表角色权限返回角色权限
    @Test
    void listRolePermissionsReturnsRolePermissions() {
        Permission permission = new Permission(9, 2, "INSERT_TAG");
        when(roleMapper.selectById(2)).thenReturn(role(2));
        when(permissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(permission));

        List<PermissionVO> permissions = roleService.listRolePermissions(2);

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getPermission()).isEqualTo("INSERT_TAG");
    }

    private Role role(Integer roleId) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName("MODERATOR");
        return role;
    }

    private Account account(Integer accountId, Integer roleId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setRoleId(roleId);
        return account;
    }
}
