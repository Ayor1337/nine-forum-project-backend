package com.ayor.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitConfigurationTest {

    private final RabbitConfiguration configuration = new RabbitConfiguration();

    @Test
    void shouldDeclareBroadcastTopologyForListener() {
        Exchange exchange = configuration.broadcastExchange();
        Queue queue = configuration.broadcastQueue();
        Binding binding = configuration.broadcastBinding(queue, exchange);

        assertEquals("broadcast.direct", exchange.getName());
        assertTrue(exchange.isDurable());
        assertEquals("broadcast.queue", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals("broadcast.direct", binding.getExchange());
        assertEquals("broadcast.queue", binding.getDestination());
        assertEquals("broadcast", binding.getRoutingKey());
    }
}
