package com.ayor.controller;

import com.ayor.entity.message.UserSystemMessage;
import com.ayor.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user_broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 通过广播交换机发送一条系统消息，用于联调消息投递链路。
     */
    @PostMapping
    public Result<Void> test(@RequestBody UserSystemMessage<String> message) {
        rabbitTemplate.convertAndSend("broadcast.direct", "broadcast", message);
        return Result.ok();
    }

}
