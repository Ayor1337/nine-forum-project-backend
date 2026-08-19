package com.ayor.controller;

import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.service.CreditService;
import com.ayor.util.SecurityUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditControllerTest {

    // 测试签到使用当前登录用户，并返回统一成功响应
    @Test
    void checkInShouldPassCurrentUserIdAndReturnSuccess() {
        CreditService creditService = mock(CreditService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CreditController controller = new CreditController(creditService, securityUtils);
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(creditService.checkIn(7)).thenReturn(null);

        Result<Void> result = controller.checkIn();

        assertEquals(ResultCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(ResultCodeEnum.SUCCESS.getMessage(), result.getMessage());
        verify(creditService).checkIn(7);
    }

    // 测试重复签到沿用统一业务失败响应
    @Test
    void checkInShouldReturnBusinessFailureForDuplicate() {
        CreditService creditService = mock(CreditService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CreditController controller = new CreditController(creditService, securityUtils);
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(creditService.checkIn(7)).thenReturn("今日已签到");

        Result<Void> result = controller.checkIn();

        assertEquals(ResultCodeEnum.FAIL.getCode(), result.getCode());
        assertEquals("今日已签到", result.getMessage());
        verify(creditService).checkIn(7);
    }
}
