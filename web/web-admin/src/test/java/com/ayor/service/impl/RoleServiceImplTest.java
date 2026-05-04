package com.ayor.service.impl;

import com.ayor.entity.pojo.Role;
import com.ayor.entity.vo.RoleVO;
import com.ayor.mapper.TopicMapper;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceImplTest {

    @Test
    void shouldReturnRoleVoWhenRoleExists() {
        TopicMapper topicMapper = (TopicMapper) Proxy.newProxyInstance(
                TopicMapper.class.getClassLoader(),
                new Class[]{TopicMapper.class},
                (proxy, method, args) -> {
                    if ("getTopicNameById".equals(method.getName())) {
                        return "后端";
                    }
                    return null;
                }
        );
        RoleServiceImpl service = new RoleServiceImpl(topicMapper) {
            @Override
            public Role getById(Serializable id) {
                return new Role(3, "ADMIN", "管理员", 1, 4);
            }
        };

        RoleVO result = service.getRoleById(3);

        assertNotNull(result);
        assertEquals(3, result.getRoleId());
        assertEquals("后端", result.getTopicName());
    }
}
