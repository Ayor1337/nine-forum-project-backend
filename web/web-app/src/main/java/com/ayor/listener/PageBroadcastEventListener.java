package com.ayor.listener;

import com.ayor.entity.message.PageBroadcastEventMessage;
import com.ayor.type.PageBroadcastScopeType;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@RabbitListener(queues = "page-broadcast.queue")
public class PageBroadcastEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitHandler
    public void onMessage(PageBroadcastEventMessage payload,
                          Message message,
                          Channel channel) throws IOException {
        messagingTemplate.convertAndSend(destination(payload.getScopeType(), payload.getScopeId()), payload);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    private String destination(PageBroadcastScopeType scopeType, Integer scopeId) {
        if (scopeType == PageBroadcastScopeType.HOME) {
            return "/broadcast/page/home";
        }
        if (scopeType == PageBroadcastScopeType.THEME) {
            return "/broadcast/page/themes/" + scopeId;
        }
        return "/broadcast/page/topics/" + scopeId;
    }
}
