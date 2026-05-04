package com.ayor.service.impl;

import com.ayor.entity.pojo.Permission;
import com.ayor.entity.vo.PermissionVO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceImplTest {

    @Test
    void shouldReturnPermissionWhenPermissionExists() {
        PermissionServiceImpl service = new PermissionServiceImpl() {
            @Override
            public Permission getById(Serializable id) {
                return new Permission(8, 2, "thread:write");
            }
        };

        PermissionVO result = service.getPermissionById(8);

        assertNotNull(result);
        assertEquals("thread:write", result.getPermission());
    }
}
