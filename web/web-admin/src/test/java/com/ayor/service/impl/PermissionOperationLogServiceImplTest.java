package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.mapper.PermissionOperationLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionOperationLogServiceImplTest {

    @Mock
    private PermissionOperationLogMapper permissionOperationLogMapper;

    private PermissionOperationLogServiceImpl permissionOperationLogService;

    @BeforeEach
    void setUp() {
        permissionOperationLogService = new PermissionOperationLogServiceImpl();
        ReflectionTestUtils.setField(permissionOperationLogService, "baseMapper", permissionOperationLogMapper);
    }

    @Test
    void listPermissionOperationLogsReturnsPagedFilteredLogVOs() {
        PermissionOperationLogVO log = new PermissionOperationLogVO();
        log.setLogId(1L);
        log.setUserId(9);
        log.setUsername("admin");
        log.setParams(Map.of("topicId", 7, "name", "java"));
        when(permissionOperationLogMapper.countPermissionOperationLogs("GRANT_ROLE", "admin", "role", 7L))
                .thenReturn(1L);
        when(permissionOperationLogMapper.selectPermissionOperationLogs(
                10,
                10,
                "GRANT_ROLE",
                "admin",
                "role",
                7L,
                true))
                .thenReturn(List.of(log));

        PageEntity<PermissionOperationLogVO> page = permissionOperationLogService.listPermissionOperationLogs(
                2,
                10,
                "GRANT_ROLE",
                "admin",
                "role",
                7L,
                "asc");

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(log);
        assertThat(page.getData().get(0).getUserId()).isEqualTo(9);
        assertThat(page.getData().get(0).getUsername()).isEqualTo("admin");
        assertThat(page.getData().get(0).getParams()).containsEntry("topicId", 7);
        verify(permissionOperationLogMapper).countPermissionOperationLogs("GRANT_ROLE", "admin", "role", 7L);
        verify(permissionOperationLogMapper).selectPermissionOperationLogs(
                10,
                10,
                "GRANT_ROLE",
                "admin",
                "role",
                7L,
                true);
    }

    @Test
    void listPermissionOperationLogsUsesDefaultsAndDescendingOrder() {
        when(permissionOperationLogMapper.countPermissionOperationLogs(null, null, null, null))
                .thenReturn(0L);
        when(permissionOperationLogMapper.selectPermissionOperationLogs(0, 10, null, null, null, null, false))
                .thenReturn(List.of());

        PageEntity<PermissionOperationLogVO> page = permissionOperationLogService.listPermissionOperationLogs(
                0,
                -1,
                null,
                null,
                null,
                null,
                "unknown");

        assertThat(page.getTotalSize()).isZero();
        assertThat(page.getData()).isEmpty();
        verify(permissionOperationLogMapper).selectPermissionOperationLogs(0, 10, null, null, null, null, false);
    }

    @Test
    void listUsernameOptionsReturnsMapperUsernames() {
        when(permissionOperationLogMapper.selectUsernameOptions()).thenReturn(List.of("admin", "operator"));

        List<String> usernames = permissionOperationLogService.listUsernameOptions();

        assertThat(usernames).containsExactly("admin", "operator");
        verify(permissionOperationLogMapper).selectUsernameOptions();
    }
}
