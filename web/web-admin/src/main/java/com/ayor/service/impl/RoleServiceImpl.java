package com.ayor.service.impl;

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


}
