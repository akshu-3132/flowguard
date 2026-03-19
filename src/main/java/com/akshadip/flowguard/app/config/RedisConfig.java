package com.akshadip.flowguard.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig - Pure configuration class for Redis beans
 * Author: Akshadip
 * 
 * This configuration class has no dependencies on other beans, ensuring it can be
 * initialized independently without creating circular dependency issues.
 */
@Slf4j
@Configuration
public class RedisConfig {

  @Value("${spring.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.redis.port:6379}")
  private Integer redisPort;

  /**
   * Creates and configures the Redis connection factory
   * 
   * @return configured RedisConnectionFactory
   */
  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    log.info(String.format("Configuring Redis connection factory for %s:%d", redisHost, redisPort));
    
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
    LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
    
    return factory;
  }

  /**
   * Creates and configures the RedisTemplate for String-Long operations
   * 
   * @param redisConnectionFactory the Redis connection factory bean
   * @return configured RedisTemplate
   */
  @Bean
  public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    log.info("Initializing RedisTemplate with StringRedisSerializer");
    
    RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());

    redisTemplate.afterPropertiesSet();

    log.debug("RedisTemplate initialized successfully");
    return redisTemplate;
  }
}
