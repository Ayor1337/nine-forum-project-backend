package com.ayor.controller;

import com.ayor.entity.pojo.Permission;
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

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/list")
    public Result<List<Permission>> listPermissions(@RequestParam(value = "role_id", required = false) Integer roleId) {
        return Result.dataMessageHandler(() -> permissionService.listPermissions(roleId), "获取权限列表失败");
    }

    @PostMapping
    public Result<Void> createPermission(@RequestBody Permission permission) {
        return Result.messageHandler(() -> permissionService.createPermission(permission));
    }

    @PutMapping("/{permission_id}")
    public Result<Void> updatePermission(@PathVariable("permission_id") Integer permissionId,
                                         @RequestBody Permission permission) {
        permission.setPermissionId(permissionId);
        return Result.messageHandler(() -> permissionService.updatePermission(permission));
    }

    @DeleteMapping("/{permission_id}")
    public Result<Void> deletePermission(@PathVariable("permission_id") Integer permissionId) {
        return Result.messageHandler(() -> permissionService.deletePermission(permissionId));
    }
}
