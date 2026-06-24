package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.FollowMessageVO;
import com.ayor.result.Result;
import com.ayor.service.FollowMessageService;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow-messages")
@RequiredArgsConstructor
@Tag(name = "关注消息")
public class FollowMessageController {

    private final FollowMessageService followMessageService;

    private final SecurityUtils securityUtils;

    @Operation(summary = "获取当前用户的关注动态消息")
    @GetMapping
    public Result<PageEntity<FollowMessageVO>> getFollowMessages(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                                 @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> followMessageService.listFollowMessages(pageNum, pageSize, userId), "获取关注消息失败");
    }
}
