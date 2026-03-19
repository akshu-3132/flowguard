package com.akshadip.flowguard.app.service;

import com.akshadip.flowguard.app.interceptor.exception.FlowLimitExceededException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * RateLimiterService - Handles Redis-based rate limiting operations
 * Author: Akshadip
 * 
 * This service encapsulates all rate limiting logic and Redis operations,
 * eliminating circular dependencies by acting as an intermediary between
 * the HTTP interceptor and Redis configuration.
 */
@Slf4j
@Service
public class RateLimiterService {

  private static final Integer DEFAULT_REDIS_EXPIRATION_DURATION = 5; // minutes
  private final RedisTemplate<String, Long> redisTemplate;
  private final Integer maxRequestPerMinute;

  /**
   * Constructor injection to ensure dependencies are resolved before service instantiation
   */
  public RateLimiterService(
      RedisTemplate<String, Long> redisTemplate,
      @Value("${flowguard.max.request}") Integer maxRequestPerMinute) {
    this.redisTemplate = redisTemplate;
    this.maxRequestPerMinute = maxRequestPerMinute;
  }

  /**
   * Validates if a request from the given IP has exceeded the rate limit.
   * Uses a weighted average algorithm considering current and last minute request counts.
   * 
   * @param ip the IP address to validate
   * @throws FlowLimitExceededException if the rate limit is exceeded
   */
  public void validateFlowLimitForIp(String ip) throws FlowLimitExceededException {
    
    LocalDateTime now = LocalDateTime.now();
    Integer elapsedSeconds = now.getSecond();
    Integer currentMinute = now.getMinute();

    // get modulo 60 so at 0 minute we get 59 (from the previous hour)
    Integer lastMinute = (currentMinute + 59) % 60;

    Long requestCountLastMinute = redisTemplate.opsForValue().get(constructRedisKey(ip, lastMinute));

    if (requestCountLastMinute == null) {
      requestCountLastMinute = 0L;
    }

    // increase the count for current minute
    Long requestCountCurrentMinute = redisTemplate.opsForValue().increment(constructRedisKey(ip, currentMinute));
    
    // set expiration for the key as Redis does not support setting timeout together with increment operation
    redisTemplate.expire(
        constructRedisKey(ip, currentMinute),
        DEFAULT_REDIS_EXPIRATION_DURATION,
        TimeUnit.MINUTES);

    // calculate weighted average
    Double weightedAvg = ((double) elapsedSeconds * requestCountCurrentMinute.doubleValue()
        + (double) (59 - elapsedSeconds) * requestCountLastMinute.doubleValue()) / 59.0;

    if (weightedAvg > maxRequestPerMinute) {
      log.warn("Rate limit exceeded for IP: {} with weighted average: {}", ip, weightedAvg);
      // Decrement the counter since we're rejecting this request
      redisTemplate.opsForValue().decrement(constructRedisKey(ip, currentMinute));
      throw new FlowLimitExceededException();
    }
    
    log.debug("IP: {} passed rate limit check with weighted average: {}", ip, weightedAvg);
  }

  /**
   * Constructs a Redis key for storing request counts per IP and minute
   * 
   * @param ip the IP address
   * @param minute the minute to track
   * @return the Redis key
   */
  private String constructRedisKey(String ip, Integer minute) {
    return String.format("count#%s#%d", ip, minute);
  }
}
