package com.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "booking.email.queue";
    public static final String EXCHANGE = "booking.exchange";
    public static final String ROUTING_KEY = "booking.confirmed";

    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding queueBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(bookingExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonConverter());
        return factory;
    }
}
