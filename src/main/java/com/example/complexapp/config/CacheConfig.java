package com.example.complexapp.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // Configure default cache settings
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        // Configure specific cache settings
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - longer TTL for user data
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Product cache - shorter TTL for frequently changing data
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("product-details", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("product-categories", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Order cache - very short TTL for transactional data
        cacheConfigurations.put("orders", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("order-items", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Search cache - medium TTL for search results
        cacheConfigurations.put("search-results", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("search-suggestions", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Session cache - longer TTL for session data
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Rate limiting cache - very short TTL
        cacheConfigurations.put("rate-limits", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }
}
