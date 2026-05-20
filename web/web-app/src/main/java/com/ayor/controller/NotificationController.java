package com.ayor.controller;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
import com.ayor.result.Result;
import com.ayor.service.MessageUnreadService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final MessageUnreadService messageUnreadService;

    private final SecurityUtils securityUtils;
    /**
     * 获取当前用户的未读消息统计。
     *
     * @param type 可选消息类型；传入后仅返回该类型的未读数，不传则返回全部汇总
     * @return 未读消息对象
     */

    @GetMapping("/unread-count")
    public Result<MessageUnread> getNotification(@RequestParam(value = "type", required = false) String type) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> {
            if (type == null) {
                return messageUnreadService.getUnreadVO(userId);
            } else {
                return messageUnreadService.getUnreadVO(userId, type);
            }
        }, "获取未读消息失败");
    }

    @GetMapping("/unread-overview")
    public Result<UnreadOverviewVO> getUnreadOverview() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> messageUnreadService.getUnreadOverviewVO(userId),
                "获取未读消息概览失败");
    }

}
