package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.pojo.Role;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(roleMapper);
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
}
