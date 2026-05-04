package com.ayor.listener;

import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.service.ReportService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@RabbitListener(queues = "report.queue")
public class ReportListener {

    private final ReportService reportService;

    @RabbitHandler
    public void onMessage(ReportCreatedMessage payload,
                          Message message,
                          Channel channel) throws IOException {
        reportService.createFromMessage(payload);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
