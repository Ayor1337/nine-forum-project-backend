package com.ayor.service.impl;

import com.ayor.entity.admin.dto.RoleDTO;
import com.ayor.entity.admin.vo.RoleVO;
import com.ayor.entity.pojo.Role;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final TopicMapper topicMapper;

    @Override
    public List<RoleVO> getRoles() {
        List<Role> roles = this.lambdaQuery().list();
        List<RoleVO> roleVos = new ArrayList<>();
        roles.forEach(role -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(role, roleVO);
            roleVO.setTopicName(topicMapper.getTopicNameById(role.getTopicId()));
            roleVos.add(roleVO);
        });
        return roleVos;
    }

    @Override
    public String createRole(RoleDTO roleDTO) {
        if (roleDTO == null || !StringUtils.hasText(roleDTO.getRoleName())) {
            return "角色名称不能为空";
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        return this.save(role) ? null : "创建角色失败";
    }

    @Override
    public String updateRole(RoleDTO roleDTO) {
        if (roleDTO == null || roleDTO.getRoleId() == null) {
            return "角色不存在";
        }
        Role role = this.getById(roleDTO.getRoleId());
        if (role == null) {
            return "角色不存在";
        }
        BeanUtils.copyProperties(roleDTO, role);
        return this.updateById(role) ? null : "更新角色失败";
    }

    @Override
    public String deleteRole(Integer roleId) {
        if (roleId == null) {
            return "角色不存在";
        }
        return this.removeById(roleId) ? null : "删除角色失败";
    }

}
