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

    @Bean("broadcast_exchange")
    public Exchange broadcastExchange() {
        return ExchangeBuilder
                .directExchange("broadcast.direct")
                .build();
    }

    @Bean("broadcast_queue")
    public Queue broadcastQueue() {
        return QueueBuilder
                .durable("broadcast.queue")
                .build();
    }

    @Bean
    public Binding broadcastBinding(@Qualifier("broadcast_queue") Queue queue,
                                    @Qualifier("broadcast_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("broadcast")
                .noargs();
    }

    @Bean
    public MessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }



}
