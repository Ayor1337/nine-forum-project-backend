package com.ayor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    /**
     * 创建邮件交换机。
     *
     * @return 交换机
     */
    @Bean("mail_exchange")
    public Exchange mailExchange() {
        return ExchangeBuilder
                .directExchange("mail.direct")
                .build();
    }

    /**
     * 创建邮件队列。
     *
     * @return 队列
     */
    @Bean("mail_queue")
    public Queue mailQueue() {
        return QueueBuilder
                .nonDurable("mail.queue")
                .build();
    }

    /**
     * 绑定邮件交换机和邮件队列。
     *
     * @param queue 邮件队列
     * @param exchange 邮件交换机
     * @return 绑定关系
     */
    @Bean("mail_binding")
    public Binding mailBinding(@Qualifier("mail_queue") Queue queue,
                               @Qualifier("mail_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("mail")
                .noargs();
    }

    @Bean("report_exchange")
    public Exchange reportExchange() {
        return ExchangeBuilder
                .directExchange("report.direct")
                .durable(true)
                .build();
    }

    @Bean("report_queue")
    public Queue reportQueue() {
        return QueueBuilder
                .durable("report.queue")
                .build();
    }

    @Bean("report_binding")
    public Binding reportBinding(@Qualifier("report_queue") Queue queue,
                                 @Qualifier("report_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("report.created")
                .noargs();
    }

    @Bean("page_broadcast_exchange")
    public Exchange pageBroadcastExchange() {
        return ExchangeBuilder
                .directExchange("page-broadcast.direct")
                .durable(true)
                .build();
    }

    @Bean("page_broadcast_queue")
    public Queue pageBroadcastQueue() {
        return QueueBuilder
                .durable("page-broadcast.queue")
                .build();
    }

    @Bean("page_broadcast_binding")
    public Binding pageBroadcastBinding(@Qualifier("page_broadcast_queue") Queue queue,
                                        @Qualifier("page_broadcast_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("page-broadcast.changed")
                .noargs();
    }

    /**
     * 创建 JSON 消息转换器。
     *
     * @param objectMapper JSON ObjectMapper
     * @return 消息转换器
     */
    @Bean
    public MessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
