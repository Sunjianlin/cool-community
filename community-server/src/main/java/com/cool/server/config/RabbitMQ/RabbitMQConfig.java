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

import java.util.HashMap;
import java.util.Map;

import static com.cool.common.constant.RabbitMQNotifyConstants.*;

@Configuration
public class RabbitMQConfig {

    // ========== 1. 消息转换器（JSON序列化） ==========
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ========== 2. 消费者容器工厂（配置限流） ==========
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(10);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setAutoStartup(true);
        return factory;
    }

    // ========== 3. 定义直连交换机（持久化） ==========
    @Bean
    public DirectExchange notifyDirectExchange() {
        return new DirectExchange(NOTIFY_EXCHANGE, true, false);
    }

    // ========== 4. 定义死信交换机（持久化） ==========
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    // ========== 5. 定义业务队列（带死信配置） ==========
    private Queue createQueueWithDLX(String queueName, String dlqRoutingKey) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return QueueBuilder.durable(queueName).withArguments(args).build();
    }

    @Bean
    public Queue commentNotifyQueue() {
        return createQueueWithDLX(COMMENT_QUEUE, COMMENT_DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue likeNotifyQueue() {
        return createQueueWithDLX(LIKE_QUEUE, LIKE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue followNotifyQueue() {
        return createQueueWithDLX(FOLLOW_QUEUE, FOLLOW_DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue privateNotifyQueue() {
        return createQueueWithDLX(PRIVATE_QUEUE, PRIVATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue systemNotifyQueue() {
        return createQueueWithDLX(SYSTEM_QUEUE, SYSTEM_DLQ_ROUTING_KEY);
    }

    // ========== 6. 定义死信队列 ==========
    @Bean
    public Queue commentDeadLetterQueue() {
        return QueueBuilder.durable(COMMENT_DLQ).build();
    }

    @Bean
    public Queue likeDeadLetterQueue() {
        return QueueBuilder.durable(LIKE_DLQ).build();
    }

    @Bean
    public Queue followDeadLetterQueue() {
        return QueueBuilder.durable(FOLLOW_DLQ).build();
    }

    @Bean
    public Queue privateDeadLetterQueue() {
        return QueueBuilder.durable(PRIVATE_DLQ).build();
    }

    @Bean
    public Queue systemDeadLetterQueue() {
        return QueueBuilder.durable(SYSTEM_DLQ).build();
    }

    // ========== 7. 绑定业务队列到交换机 ==========
    @Bean
    public Binding commentBinding(Queue commentNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(commentNotifyQueue)
                .to(notifyDirectExchange)
                .with(COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding likeBinding(Queue likeNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(likeNotifyQueue)
                .to(notifyDirectExchange)
                .with(LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding followBinding(Queue followNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(followNotifyQueue)
                .to(notifyDirectExchange)
                .with(FOLLOW_ROUTING_KEY);
    }

    @Bean
    public Binding privateBinding(Queue privateNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(privateNotifyQueue)
                .to(notifyDirectExchange)
                .with(PRIVATE_ROUTING_KEY);
    }

    @Bean
    public Binding systemBinding(Queue systemNotifyQueue, DirectExchange notifyDirectExchange) {
        return BindingBuilder.bind(systemNotifyQueue)
                .to(notifyDirectExchange)
                .with(SYSTEM_ROUTING_KEY);
    }

    // ========== 8. 绑定死信队列到死信交换机 ==========
    @Bean
    public Binding commentDlqBinding(Queue commentDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(commentDeadLetterQueue)
                .to(deadLetterExchange)
                .with(COMMENT_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding likeDlqBinding(Queue likeDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(likeDeadLetterQueue)
                .to(deadLetterExchange)
                .with(LIKE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding followDlqBinding(Queue followDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(followDeadLetterQueue)
                .to(deadLetterExchange)
                .with(FOLLOW_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding privateDlqBinding(Queue privateDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(privateDeadLetterQueue)
                .to(deadLetterExchange)
                .with(PRIVATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding systemDlqBinding(Queue systemDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(systemDeadLetterQueue)
                .to(deadLetterExchange)
                .with(SYSTEM_DLQ_ROUTING_KEY);
    }
}
