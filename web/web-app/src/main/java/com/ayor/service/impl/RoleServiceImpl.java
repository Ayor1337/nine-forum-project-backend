package com.ayor.service.impl;

import com.ayor.entity.pojo.Role;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
