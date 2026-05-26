package com.ayor.service.impl;

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
    void listPermissionOperationLogsReturnsLatestLogVOs() {
        PermissionOperationLogVO log = new PermissionOperationLogVO();
        log.setLogId(1L);
        log.setUsername("admin");
        log.setParams(Map.of("topicId", 7, "name", "java"));
        when(permissionOperationLogMapper.listLatest()).thenReturn(List.of(log));

        List<PermissionOperationLogVO> logs = permissionOperationLogService.listPermissionOperationLogs();

        assertThat(logs).containsExactly(log);
        assertThat(logs.get(0).getUsername()).isEqualTo("admin");
        assertThat(logs.get(0).getParams()).containsEntry("topicId", 7);
        verify(permissionOperationLogMapper).listLatest();
    }
}
