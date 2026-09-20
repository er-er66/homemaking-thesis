package com.example.homemaking.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();//在java中创建一个RedisTemplate的工具类
        template.setConnectionFactory(factory);//给工具实例，设置连接工厂

        // JSON序列化配置
        ObjectMapper objectMapper = new ObjectMapper();//创建一个ObjectMapper实例
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);//设置属性可见性为任意访问
        //PropertyAccessor.ALL：设置访问类型为任意访问，如get set 字段，布尔
        //JsonAutoDetect.Visibility.ANY 设置任意访问权限都可以访问，如public，protected,private等都可以访问

        objectMapper.registerModule(new JavaTimeModule());//注册JavaTime模块
        //因为java8+中Redis不自持LocalDateTime和LocalTime,如果不注册就放入到Redis就会报错，抛异常

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);//禁用日期写入为时间戳

        // 序列化器
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 设置序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}