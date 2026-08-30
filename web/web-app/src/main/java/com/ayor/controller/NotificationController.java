package com.ayor.controller;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
import com.ayor.result.Result;
import com.ayor.service.MessageUnreadService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知")
public class NotificationController {

    private final MessageUnreadService messageUnreadService;

    private final SecurityUtils securityUtils;
    /**
     * 获取当前用户的未读消息统计。
     */
    @Operation(summary = "获取当前用户的未读消息统计")
    @GetMapping("/unread-count")
    public Result<MessageUnread> getNotification(@Parameter(description = "消息类型") @RequestParam(value = "type", required = false) String type) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> {
            if (type == null) {
                return messageUnreadService.getUnreadVO(userId);
            } else {
                return messageUnreadService.getUnreadVO(userId, type);
            }
        }, "获取未读消息失败");
    }

    @Operation(summary = "获取当前用户的未读消息概览")
    @GetMapping("/unread-overview")
    public Result<UnreadOverviewVO> getUnreadOverview() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> messageUnreadService.getUnreadOverviewVO(userId),
                "获取未读消息概览失败");
    }

}
