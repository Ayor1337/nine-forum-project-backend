package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.result.Result;
import com.ayor.service.PermissionOperationLogService;
import com.ayor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "权限操作日志", description = "后台权限操作日志查询接口")
@RestController
@RequestMapping("/api/permission_operation_logs")
@RequiredArgsConstructor
public class PermissionOperationLogController {

    private final PermissionOperationLogService permissionOperationLogService;

    private final PermissionService permissionService;

    @Operation(summary = "查询后台资源")
    @GetMapping
    public Result<PageEntity<PermissionOperationLogVO>> listPermissionOperationLogs(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "action") @RequestParam(value = "action", required = false) String action,
            @Parameter(description = "用户名") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "targetType") @RequestParam(value = "target_type", required = false) String targetType,
            @Parameter(description = "targetId") @RequestParam(value = "target_id", required = false) Long targetId,
            @Parameter(description = "sortOrder") @RequestParam(value = "sort_order", defaultValue = "desc", required = false) String sortOrder) {
        return Result.dataMessageHandler(
                () -> permissionOperationLogService.listPermissionOperationLogs(
                        pageNum,
                        pageSize,
                        action,
                        username,
                        targetType,
                        targetId,
                        sortOrder),
                "获取权限操作日志失败"
        );
    }

    /**
     * 获取可分配权限标识选项。
     */
    @Operation(summary = "获取可分配权限标识选项")
    @GetMapping("/operation/options")
    public Result<List<String>> listPermissionOptions() {
        return Result.dataMessageHandler(permissionService::listPermissionOptions, "获取权限选项失败");
    }

    /**
     * 获取权限操作日志用户名筛选选项。
     */
    @Operation(summary = "获取权限操作日志用户名筛选选项")
    @GetMapping("/username/options")
    public Result<List<String>> listUsernameOptions() {
        return Result.dataMessageHandler(permissionOperationLogService::listUsernameOptions, "获取权限操作用户选项失败");
    }
}
