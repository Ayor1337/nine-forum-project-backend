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
     * 定义广播消息使用的直连交换机。
     */
    @Bean("broadcast_exchange")
    public Exchange broadcastExchange() {
        return ExchangeBuilder
                .directExchange("broadcast.direct")
                .build();
    }

    /**
     * 定义广播消息队列。
     */
    @Bean("broadcast_queue")
    public Queue broadcastQueue() {
        return QueueBuilder
                .durable("broadcast.queue")
                .build();
    }

    /**
     * 绑定广播队列和交换机，使指定 routing key 的消息能够进入队列。
     */
    @Bean
    public Binding broadcastBinding(@Qualifier("broadcast_queue") Queue queue,
                                    @Qualifier("broadcast_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("broadcast")
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
     * 使用 Jackson 将 RabbitMQ 消息体序列化为 JSON。
     */
    @Bean
    public MessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }



}
