package com.ayor.controller;

import com.ayor.entity.admin.dto.RoleDTO;
import com.ayor.entity.admin.vo.RoleVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    @GetMapping("/list")
    public Result<List<RoleVO>> getRoles() {
        return Result.dataMessageHandler(roleService::getRoles, "获取角色列表失败");
    }

    @PostMapping
    public Result<Void> createRole(@RequestBody RoleDTO roleDTO) {
        return Result.messageHandler(() -> roleService.createRole(roleDTO));
    }

    @PutMapping
    public Result<Void> updateRole(@RequestBody RoleDTO roleDTO) {
        return Result.messageHandler(() -> roleService.updateRole(roleDTO));
    }

    @DeleteMapping("/{role_id}")
    public Result<Void> deleteRole(@PathVariable("role_id") Integer roleId) {
        return Result.messageHandler(() -> roleService.deleteRole(roleId));
    }


}
