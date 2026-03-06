package com.cool.server.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "notification.exchange";

    public static final String PRIVATE_MESSAGE_QUEUE = "notification.private";
    public static final String COMMENT_REPLY_QUEUE = "notification.comment.reply";
    public static final String POST_COMMENT_QUEUE = "notification.post.comment";
    public static final String SYSTEM_NOTIFICATION_QUEUE = "notification.system";

    public static final String PRIVATE_MESSAGE_ROUTING_KEY = "notification.private";
    public static final String COMMENT_REPLY_ROUTING_KEY = "notification.comment.reply";
    public static final String POST_COMMENT_ROUTING_KEY = "notification.post.comment";
    public static final String SYSTEM_NOTIFICATION_ROUTING_KEY = "notification.system";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue privateMessageQueue() {
        return QueueBuilder.durable(PRIVATE_MESSAGE_QUEUE).build();
    }

    @Bean
    public Queue commentReplyQueue() {
        return QueueBuilder.durable(COMMENT_REPLY_QUEUE).build();
    }

    @Bean
    public Queue postCommentQueue() {
        return QueueBuilder.durable(POST_COMMENT_QUEUE).build();
    }

    @Bean
    public Queue systemNotificationQueue() {
        return QueueBuilder.durable(SYSTEM_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding privateMessageBinding() {
        return BindingBuilder.bind(privateMessageQueue())
                .to(notificationExchange())
                .with(PRIVATE_MESSAGE_ROUTING_KEY);
    }

    @Bean
    public Binding commentReplyBinding() {
        return BindingBuilder.bind(commentReplyQueue())
                .to(notificationExchange())
                .with(COMMENT_REPLY_ROUTING_KEY);
    }

    @Bean
    public Binding postCommentBinding() {
        return BindingBuilder.bind(postCommentQueue())
                .to(notificationExchange())
                .with(POST_COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding systemNotificationBinding() {
        return BindingBuilder.bind(systemNotificationQueue())
                .to(notificationExchange())
                .with(SYSTEM_NOTIFICATION_ROUTING_KEY);
    }
}
