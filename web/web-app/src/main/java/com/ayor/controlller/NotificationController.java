package com.ayor.controlller;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.result.Result;
import com.ayor.service.MessageUnreadService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notif")
@RequiredArgsConstructor
public class NotificationController {

    private final MessageUnreadService messageUnreadService;

    private final SecurityUtils securityUtils;

    @GetMapping("/remaining_message_unread")
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

}
