package com.ayor.controller;

import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.result.Result;
import com.ayor.service.PermissionOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permission_operation_logs")
@RequiredArgsConstructor
public class PermissionOperationLogController {

    private final PermissionOperationLogService permissionOperationLogService;

    @GetMapping
    public Result<List<PermissionOperationLogVO>> listPermissionOperationLogs() {
        return Result.dataMessageHandler(
                permissionOperationLogService::listPermissionOperationLogs,
                "获取权限操作日志失败"
        );
    }
}
