package com.ayor.controller;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.result.Result;
import com.ayor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "权限管理", description = "后台权限配置管理接口")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 查询某个角色的权限列表。
     */
    @Operation(summary = "查询某个角色的权限列表")
    @GetMapping
    public Result<List<PermissionVO>> listPermissions(@Parameter(description = "角色ID") @RequestParam(value = "role_id", required = false) Integer roleId) {
        return Result.dataMessageHandler(() -> permissionService.listPermissions(roleId), "获取权限列表失败");
    }

    /**
     * 查询单条权限记录。
     */
    @Operation(summary = "查询单条权限记录")
    @GetMapping("/{permissionId}")
    public Result<PermissionVO> getPermission(@Parameter(description = "权限ID") @PathVariable("permissionId") Integer permissionId) {
        return Result.dataMessageHandler(() -> permissionService.getPermissionById(permissionId), "获取权限失败");
    }

    /**
     * 创建权限记录。
     */
    @Operation(summary = "创建权限记录")
    @PostMapping
    public Result<Void> createPermission(@Parameter(description = "权限信息") @RequestBody Permission permission) {
        return Result.messageHandler(() -> permissionService.createPermission(permission));
    }

    /**
     * 更新指定权限。
     */
    @Operation(summary = "更新指定权限")
    @PutMapping("/{permissionId}")
    public Result<Void> updatePermission(@Parameter(description = "权限ID") @PathVariable("permissionId") Integer permissionId,
                                         @Parameter(description = "权限信息") @RequestBody Permission permission) {
        permission.setPermissionId(permissionId);
        return Result.messageHandler(() -> permissionService.updatePermission(permission));
    }

    /**
     * 批量更新权限。
     */
    @Operation(summary = "批量更新权限")
    @PutMapping("/batch")
    public Result<Void> updatePermissions(@Parameter(description = "List<Permission>") @RequestBody List<Permission> permissions) {
        return Result.messageHandler(() -> permissionService.updatePermissions(permissions));
    }

    /**
     * 删除指定权限。
     */
    @Operation(summary = "删除指定权限")
    @DeleteMapping("/{permissionId}")
    public Result<Void> deletePermission(@Parameter(description = "权限ID") @PathVariable("permissionId") Integer permissionId) {
        return Result.messageHandler(() -> permissionService.deletePermission(permissionId));
    }

    /**
     * 批量删除权限。
     */
    @Operation(summary = "批量删除权限")
    @DeleteMapping("/batch")
    public Result<Void> deletePermissions(@Parameter(description = "List<Integer>") @RequestBody List<Integer> permissionIds) {
        return Result.messageHandler(() -> permissionService.deletePermissions(permissionIds));
    }
}
