package com.ayor.service.impl;

import com.ayor.entity.dto.AccountDTO;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceImplTest {

    @Test
    void shouldEvictUserInfoCacheAfterAccountMutations() throws NoSuchMethodException {
        assertUserInfoEvict(AccountServiceImpl.class.getMethod("violationProfile", Integer.class, String.class), "#accountId");
        assertUserInfoEvict(AccountServiceImpl.class.getMethod("updateAccount", AccountDTO.class), "#accountDTO.accountId");
        assertUserInfoEvict(AccountServiceImpl.class.getMethod("deleteAccount", Integer.class), "#accountId");
    }

    private void assertUserInfoEvict(Method method, String key) {
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        assertNotNull(cacheEvict, method.getName() + " 应声明用户缓存失效");
        assertArrayEquals(new String[]{"userInfo"}, cacheEvict.value());
        assertEquals(key, cacheEvict.key());
    }
}
