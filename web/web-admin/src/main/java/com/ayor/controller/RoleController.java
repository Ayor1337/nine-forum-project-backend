package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.RoleDTO;
import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.AccountVO;
import com.ayor.entity.vo.PermissionVO;
import com.ayor.entity.vo.RoleVO;
import com.ayor.result.Result;
import com.ayor.service.RoleService;
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

@Tag(name = "角色管理", description = "后台角色、用户角色和角色权限管理接口")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    /**
     * 查询全部角色及其关联话题。
     */
    @Operation(summary = "查询全部角色及其关联话题")
    @GetMapping
    public Result<List<RoleVO>> getRoles() {
        return Result.dataMessageHandler(roleService::getRoles, "获取角色列表失败");
    }

    /**
     * 查询单个角色详情。
     */
    @Operation(summary = "查询单个角色详情")
    @GetMapping("/{roleId}")
    public Result<RoleVO> getRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId) {
        return Result.dataMessageHandler(() -> roleService.getRoleById(roleId), "获取角色失败");
    }

    /**
     * 创建角色。
     */
    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Void> createRole(@Parameter(description = "角色信息") @RequestBody RoleDTO roleDTO) {
        return Result.messageHandler(() -> roleService.createRole(roleDTO));
    }

    /**
     * 更新指定角色。
     */
    @Operation(summary = "更新指定角色")
    @PutMapping("/{roleId}")
    public Result<Void> updateRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId, @RequestBody RoleDTO roleDTO) {
        roleDTO.setRoleId(roleId);
        return Result.messageHandler(() -> roleService.updateRole(roleDTO));
    }

    /**
     * 删除指定角色。
     */
    @Operation(summary = "删除指定角色")
    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId) {
        return Result.messageHandler(() -> roleService.deleteRole(roleId));
    }

    @Operation(summary = "执行后台管理操作")
    @GetMapping("/{roleId}/accounts")
    public Result<PageEntity<AccountVO>> listRoleAccounts(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId,
                                                          @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                          @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> roleService.listRoleAccounts(roleId, pageNum, pageSize), "获取角色用户失败");
    }

    @Operation(summary = "执行后台管理操作")
    @PostMapping("/{roleId}/accounts/{accountId}")
    public Result<Void> addAccountToRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId,
                                         @Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> roleService.addAccountToRole(roleId, accountId));
    }

    @Operation(summary = "执行后台管理操作")
    @DeleteMapping("/{roleId}/accounts/{accountId}")
    public Result<Void> removeAccountFromRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId,
                                              @Parameter(description = "用户ID") @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> roleService.removeAccountFromRole(roleId, accountId));
    }

    @Operation(summary = "执行后台管理操作")
    @GetMapping("/{roleId}/permissions")
    public Result<List<PermissionVO>> listRolePermissions(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId) {
        return Result.dataMessageHandler(() -> roleService.listRolePermissions(roleId), "获取角色权限失败");
    }

    @Operation(summary = "执行后台管理操作")
    @PostMapping("/{roleId}/permissions")
    public Result<Void> addPermissionToRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId,
                                            @Parameter(description = "权限信息") @RequestBody Permission permission) {
        String permissionName = permission == null ? null : permission.getPermission();
        return Result.messageHandler(() -> roleService.addPermissionToRole(roleId, permissionName));
    }

    @Operation(summary = "执行后台管理操作")
    @DeleteMapping("/{roleId}/permissions/{permission}")
    public Result<Void> removePermissionFromRole(@Parameter(description = "角色ID") @PathVariable("roleId") Integer roleId,
                                                 @Parameter(description = "权限标识") @PathVariable("permission") String permission) {
        return Result.messageHandler(() -> roleService.removePermissionFromRole(roleId, permission));
    }

}
