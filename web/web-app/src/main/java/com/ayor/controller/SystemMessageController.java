package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.SystemMessageVO;
import com.ayor.result.Result;
import com.ayor.service.SystemMessageService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-messages")
@RequiredArgsConstructor
@Tag(name = "系统消息")
public class SystemMessageController {

    private final SystemMessageService systemMessageService;

    private final SecurityUtils securityUtils;
    /**
     * 获取当前用户的系统消息列表。
     */
    @Operation(summary = "获取当前用户的系统消息列表")
    @GetMapping
    public Result<PageEntity<SystemMessageVO>> getSystemMessages(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                                   @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> systemMessageService.listSystemMessage(pageNum, pageSize, userId), "获取系统消息成功");
    }

}
