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

    @Bean("mail_exchange")
    public Exchange mailExchange() {
        return ExchangeBuilder
                .directExchange("mail.direct")
                .build();
    }

    @Bean("mail_queue")
    public Queue mailQueue() {
        return QueueBuilder
                .nonDurable("mail.queue")
                .build();
    }

    @Bean("mail_binding")
    public Binding mailBinding(@Qualifier("mail_queue") Queue queue,
                               @Qualifier("mail_exchange") Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("mail")
                .noargs();
    }

    @Bean
    public MessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
