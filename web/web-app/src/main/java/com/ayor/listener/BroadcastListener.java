package com.ayor.listener;

import com.ayor.entity.message.BroadcastMessage;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.service.BroadcastService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "broadcast.queue")
@Slf4j
@RequiredArgsConstructor
public class BroadcastListener {

    private final BroadcastService broadcastService;

    /**
     * 消费广播消息并转发到对应的广播服务。
     *
     * @param payload 广播消息
     * @param message RabbitMQ 消息体
     * @param channel RabbitMQ 通道
     * @param <T> 消息载荷类型
     * @throws IOException IO 异常
     */
    @RabbitHandler
    public <T> void onMessage(BroadcastMessage<T> payload,
                              Message message,
                              Channel channel) throws IOException {
        if (payload instanceof UserSystemMessage<T> userSystemMessage) {
            broadcastService.userSystemBroadcast(userSystemMessage);
        }
        if (payload instanceof UserViolationMessage<T> userViolationMessage) {
            broadcastService.userViolationBroadcast(userViolationMessage);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
