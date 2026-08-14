package com.payflow.payflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_QUEUE = "orderQueue";
    public static final String NOTIFICATION_QUEUE = "notificationQueue";
    public static final String EXCHANGE_NAME = "payflowExchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notificationRoutingKey";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(NOTIFICATION_ROUTING_KEY);
    }
}