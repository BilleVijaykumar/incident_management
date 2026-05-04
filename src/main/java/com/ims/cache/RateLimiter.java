package com.ims.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Rate Limiter using Redis - Token bucket algorithm
 * Provides distributed rate limiting across multiple instances
 */
@Component
public class RateLimiter {
    
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;
    
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:";
    private static final String RATE_LIMIT_BURST_PREFIX = "rate:burst:";
    
    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // Lua script for atomic rate limiting
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(
            """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current = tonumber(redis.call('GET', key) or '0')
            if current < limit then
                redis.call('INCR', key)
                redis.call('EXPIRE', key, window)
                return 1
            else
                return 0
            end
            """
        );
        this.rateLimitScript.setResultType(Long.class);
    }
    
    /**
     * Check if request is allowed under rate limit
     */
    public boolean tryAcquire(String key, int limit, Duration window) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        Long result = redisTemplate.execute(
            rateLimitScript,
            List.of(redisKey),
            String.valueOf(limit),
            String.valueOf((int) window.getSeconds())
        );
        return result != null && result == 1L;
    }
    
    /**
     * Check if request is allowed with burst capacity
     */
    public boolean tryAcquireWithBurst(String key, int limit, int burst, Duration window) {
        String burstKey = RATE_LIMIT_BURST_PREFIX + key;
        
        // Check burst capacity first
        String burstValue = redisTemplate.opsForValue().get(burstKey);
        int currentBurst = burstValue != null ? Integer.parseInt(burstValue) : burst;
        
        if (currentBurst <= 0) {
            return false;
        }
        
        // Try to acquire from main rate limit
        if (tryAcquire(key, limit, window)) {
            // Decrement burst
            redisTemplate.opsForValue().decrement(burstKey);
            if (currentBurst == burst) {
                redisTemplate.opsForValue().set(burstKey, String.valueOf(burst - 1), window);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Get current rate limit count for a key
     */
    public long getCurrentCount(String key) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        return value != null ? Long.parseLong(value) : 0;
    }
    
    /**
     * Reset rate limit for a key
     */
    public void reset(String key) {
        redisTemplate.delete(RATE_LIMIT_KEY_PREFIX + key);
        redisTemplate.delete(RATE_LIMIT_BURST_PREFIX + key);
    }
}