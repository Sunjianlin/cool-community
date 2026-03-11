package com.cool.server.config.RabbitMQ;

import com.cool.common.constant.RabbitMQNotifyConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.cool.common.constant.RabbitMQNotifyConstants.*;

@Configuration
public class RabbitMQConfig {
    // ========== 1. 消息转换器（JSON序列化） ==========
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ========== 2. 定义直连交换机（持久化） ==========
    @Bean
    public DirectExchange notifyDirectExchange() {
        // 参数：名称、是否持久化、是否自动删除
        return new DirectExchange(RabbitMQNotifyConstants.NOTIFY_EXCHANGE, true, false);
    }

    // ========== 3. 定义各业务队列（持久化 + 排他性关闭 + 自动删除关闭） ==========
    @Bean
    public Queue commentNotifyQueue() {
        return QueueBuilder.durable(RabbitMQNotifyConstants.COMMENT_QUEUE).build();
    }

    @Bean
    public Queue likeNotifyQueue() {
        return QueueBuilder.durable(RabbitMQNotifyConstants.LIKE_QUEUE).build();
    }

    @Bean
    public Queue followNotifyQueue() {
        return QueueBuilder.durable(RabbitMQNotifyConstants.FOLLOW_QUEUE).build();
    }

    @Bean
    public Queue privateNotifyQueue() {
        return QueueBuilder.durable(RabbitMQNotifyConstants.PRIVATE_QUEUE).build();
    }

    @Bean
    public Queue systemNotifyQueue() {
        return QueueBuilder.durable(RabbitMQNotifyConstants.SYSTEM_QUEUE).build();
    }

    // ========== 4. 绑定队列到交换机（路由键匹配） ==========
    @Bean
    public Binding commentBinding(Queue commentNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(commentNotifyQueue)
                .to(notifyDirectExchange)
                .with(RabbitMQNotifyConstants.COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding likeBinding(Queue likeNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(likeNotifyQueue)
                .to(notifyDirectExchange)
                .with(RabbitMQNotifyConstants.LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding followBinding(Queue followNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(followNotifyQueue)
                .to(notifyDirectExchange)
                .with(RabbitMQNotifyConstants.FOLLOW_ROUTING_KEY);
    }

    @Bean
    public Binding privateBinding(Queue privateNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(privateNotifyQueue)
                .to(notifyDirectExchange)
                .with(RabbitMQNotifyConstants.PRIVATE_ROUTING_KEY);
    }

    @Bean
    public Binding systemBinding(Queue systemNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(systemNotifyQueue)
                .to(notifyDirectExchange)
                .with(RabbitMQNotifyConstants.SYSTEM_ROUTING_KEY);
    }
}
