package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.pojo.Role;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache userInfoCache;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(roleMapper, accountMapper, cacheManager);
        ReflectionTestUtils.setField(permissionService, "baseMapper", permissionMapper);
    }

    @Test
    void updatePermissionsUpdatesEveryPermission() {
        Permission first = new Permission(11, 2, "DELETE_THREAD");
        Permission second = new Permission(12, null, "DELETE_POST");
        when(permissionMapper.selectById(11)).thenReturn(new Permission(11, 3, "INSERT_TAG"));
        when(permissionMapper.selectById(12)).thenReturn(new Permission(12, 3, "INSERT_TAG"));
        when(roleMapper.selectById(2)).thenReturn(new Role());
        when(permissionMapper.updateById(any(Permission.class))).thenReturn(1);

        String message = permissionService.updatePermissions(List.of(first, second));

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionMapper, times(2)).updateById(captor.capture());
        assertThat(message).isNull();
        assertThat(captor.getAllValues())
                .extracting(Permission::getPermission)
                .containsExactly("DELETE_THREAD", "DELETE_POST");
    }

    @Test
    void listPermissionOptionsReturnsKnownPermissionValues() {
        List<String> options = permissionService.listPermissionOptions();

        assertThat(options)
                .contains("MANAGE_ROLE", "DELETE_THREAD", "MANAGE_HISTORY")
                .doesNotContain("UNKNOWN");
    }

    @Test
    void updatePermissionsRejectsEmptyList() {
        String message = permissionService.updatePermissions(List.<Permission>of());

        assertThat(message).isEqualTo("权限不能为空");
        verify(permissionMapper, never()).updateById(any(Permission.class));
    }

    @Test
    void updatePermissionsStopsWhenPermissionIsInvalid() {
        Permission first = new Permission(11, null, "UNKNOWN");
        Permission second = new Permission(12, null, "DELETE_POST");
        when(permissionMapper.selectById(11)).thenReturn(new Permission(11, 3, "INSERT_TAG"));

        String message = permissionService.updatePermissions(List.of(first, second));

        assertThat(message).isEqualTo("权限标识不存在");
        verify(permissionMapper, never()).updateById(any(Permission.class));
    }

    @Test
    void deletePermissionsDeletesEveryPermissionId() {
        when(permissionMapper.deleteById((Serializable) 11)).thenReturn(1);
        when(permissionMapper.deleteById((Serializable) 12)).thenReturn(1);

        String message = permissionService.deletePermissions(List.of(11, 12));

        assertThat(message).isNull();
        verify(permissionMapper).deleteById((Serializable) 11);
        verify(permissionMapper).deleteById((Serializable) 12);
    }

    @Test
    void deletePermissionsRejectsEmptyList() {
        String message = permissionService.deletePermissions(List.<Integer>of());

        assertThat(message).isEqualTo("权限不能为空");
        verify(permissionMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void createPermissionEvictsUserInfoCacheForUsersInPermissionRole() {
        Permission permission = new Permission(null, 2, "DELETE_THREAD");
        when(roleMapper.selectById(2)).thenReturn(new Role());
        when(permissionMapper.insert(permission)).thenReturn(1);
        when(accountMapper.getAccountIdsByRoleId(2)).thenReturn(List.of(7, 8));
        when(cacheManager.getCache("userInfo")).thenReturn(userInfoCache);

        String message = permissionService.createPermission(permission);

        assertThat(message).isNull();
        verify(userInfoCache).evict(7);
        verify(userInfoCache).evict(8);
    }

    @Test
    void updatePermissionEvictsUserInfoCacheForOriginalAndCurrentRoleUsers() {
        Permission permission = new Permission(11, 2, "DELETE_THREAD");
        when(permissionMapper.selectById(11)).thenReturn(new Permission(11, 3, "INSERT_TAG"));
        when(roleMapper.selectById(2)).thenReturn(new Role());
        when(permissionMapper.updateById(any(Permission.class))).thenReturn(1);
        when(accountMapper.getAccountIdsByRoleId(3)).thenReturn(List.of(9));
        when(accountMapper.getAccountIdsByRoleId(2)).thenReturn(List.of(7, 8));
        when(cacheManager.getCache("userInfo")).thenReturn(userInfoCache);

        String message = permissionService.updatePermission(permission);

        assertThat(message).isNull();
        verify(userInfoCache).evict(9);
        verify(userInfoCache).evict(7);
        verify(userInfoCache).evict(8);
    }

    @Test
    void deletePermissionEvictsUserInfoCacheForUsersInDeletedPermissionRole() {
        when(permissionMapper.selectById(11)).thenReturn(new Permission(11, 2, "DELETE_THREAD"));
        when(permissionMapper.deleteById((Serializable) 11)).thenReturn(1);
        when(accountMapper.getAccountIdsByRoleId(2)).thenReturn(List.of(7));
        when(cacheManager.getCache("userInfo")).thenReturn(userInfoCache);

        String message = permissionService.deletePermission(11);

        assertThat(message).isNull();
        verify(userInfoCache).evict(7);
    }
}
