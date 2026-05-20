package com.ayor.controller;

import com.ayor.entity.dto.UserBroadcastDTO;
import com.ayor.result.Result;
import com.ayor.service.UserBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user_broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final UserBroadcastService userBroadcastService;

    /**
     * 向一个或多个用户发送系统通知。
     */
    @PostMapping
    public Result<Void> sendUserBroadcast(@RequestBody UserBroadcastDTO dto) {
        return Result.messageHandler(() -> userBroadcastService.sendUserBroadcast(dto));
    }

}
