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

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    /**
     * 查询全部角色及其关联话题。
     */
    @GetMapping
    public Result<List<RoleVO>> getRoles() {
        return Result.dataMessageHandler(roleService::getRoles, "获取角色列表失败");
    }

    /**
     * 查询单个角色详情。
     */
    @GetMapping("/{roleId}")
    public Result<RoleVO> getRole(@PathVariable("roleId") Integer roleId) {
        return Result.dataMessageHandler(() -> roleService.getRoleById(roleId), "获取角色失败");
    }

    /**
     * 创建角色。
     */
    @PostMapping
    public Result<Void> createRole(@RequestBody RoleDTO roleDTO) {
        return Result.messageHandler(() -> roleService.createRole(roleDTO));
    }

    /**
     * 更新指定角色。
     */
    @PutMapping("/{roleId}")
    public Result<Void> updateRole(@PathVariable("roleId") Integer roleId, @RequestBody RoleDTO roleDTO) {
        roleDTO.setRoleId(roleId);
        return Result.messageHandler(() -> roleService.updateRole(roleDTO));
    }

    /**
     * 删除指定角色。
     */
    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable("roleId") Integer roleId) {
        return Result.messageHandler(() -> roleService.deleteRole(roleId));
    }

    @GetMapping("/{roleId}/accounts")
    public Result<PageEntity<AccountVO>> listRoleAccounts(@PathVariable("roleId") Integer roleId,
                                                          @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> roleService.listRoleAccounts(roleId, pageNum, pageSize), "获取角色用户失败");
    }

    @PostMapping("/{roleId}/accounts/{accountId}")
    public Result<Void> addAccountToRole(@PathVariable("roleId") Integer roleId,
                                         @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> roleService.addAccountToRole(roleId, accountId));
    }

    @DeleteMapping("/{roleId}/accounts/{accountId}")
    public Result<Void> removeAccountFromRole(@PathVariable("roleId") Integer roleId,
                                              @PathVariable("accountId") Integer accountId) {
        return Result.messageHandler(() -> roleService.removeAccountFromRole(roleId, accountId));
    }

    @GetMapping("/{roleId}/permissions")
    public Result<List<PermissionVO>> listRolePermissions(@PathVariable("roleId") Integer roleId) {
        return Result.dataMessageHandler(() -> roleService.listRolePermissions(roleId), "获取角色权限失败");
    }

    @PostMapping("/{roleId}/permissions")
    public Result<Void> addPermissionToRole(@PathVariable("roleId") Integer roleId,
                                            @RequestBody Permission permission) {
        String permissionName = permission == null ? null : permission.getPermission();
        return Result.messageHandler(() -> roleService.addPermissionToRole(roleId, permissionName));
    }

    @DeleteMapping("/{roleId}/permissions/{permission}")
    public Result<Void> removePermissionFromRole(@PathVariable("roleId") Integer roleId,
                                                 @PathVariable("permission") String permission) {
        return Result.messageHandler(() -> roleService.removePermissionFromRole(roleId, permission));
    }

}
