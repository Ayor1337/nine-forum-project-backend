package com.ayor.service;

import com.ayor.entity.admin.dto.RoleDTO;
import com.ayor.entity.admin.vo.RoleVO;
import com.ayor.entity.pojo.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<RoleVO> getRoles();

    String createRole(RoleDTO roleDTO);

    String updateRole(RoleDTO roleDTO);

    String deleteRole(Integer roleId);
}
