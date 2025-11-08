package com.ayor.controller;

import com.ayor.entity.admin.vo.RoleVO;
import com.ayor.result.Result;
import com.ayor.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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


}
