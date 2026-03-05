package com.cool.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class RedisConfig {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // ========== 配置ObjectMapper支持Java 8时间类型 ==========
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块（解决LocalDateTime序列化）
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 自定义LocalDateTime序列化格式（避免默认转成时间戳/数组）
        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))
        );
        objectMapper.registerModule(javaTimeModule);
        // 关闭时间戳序列化（保证时间是字符串格式，而非数字）
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 设置key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());


        // 设置hash key序列化器
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 2. Value序列化：使用配置好的ObjectMapper（原有逻辑升级）
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        // 设置value序列化器
        redisTemplate.setValueSerializer(valueSerializer);
        // 设置hash value序列化器
        redisTemplate.setHashValueSerializer(valueSerializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
