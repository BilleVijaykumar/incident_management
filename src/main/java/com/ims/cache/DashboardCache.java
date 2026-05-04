package com.ims.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dashboard Cache - Hot path data caching using Redis
 * Caches frequently accessed dashboard data
 */
@Component
public class DashboardCache {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String DASHBOARD_STATS_KEY = "dashboard:stats";
    private static final String INCIDENT_SUMMARY_KEY = "dashboard:incident:summary";
    private static final String SEVERITY_STATS_KEY = "dashboard:severity:stats";
    
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    
    public DashboardCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Cache dashboard statistics
     */
    public void cacheDashboardStats(Map<String, Object> stats) {
        // Using hash operations for structured data
        redisTemplate.opsForHash().putAll(DASHBOARD_STATS_KEY, stats);
        redisTemplate.expire(DASHBOARD_STATS_KEY, CACHE_TTL);
    }
    
    /**
     * Get cached dashboard statistics
     */
    public Map<Object, Object> getDashboardStats() {
        return redisTemplate.opsForHash().entries(DASHBOARD_STATS_KEY);
    }
    
    /**
     * Cache incident summary
     */
    public void cacheIncidentSummary(String incidentId, Map<String, Object> summary) {
        String key = INCIDENT_SUMMARY_KEY + ":" + incidentId;
        redisTemplate.opsForHash().putAll(key, summary);
        redisTemplate.expire(key, CACHE_TTL);
    }
    
    /**
     * Get cached incident summary
     */
    public Map<Object, Object> getIncidentSummary(String incidentId) {
        String key = INCIDENT_SUMMARY_KEY + ":" + incidentId;
        return redisTemplate.opsForHash().entries(key);
    }
    
    /**
     * Cache severity statistics
     */
    public void cacheSeverityStats(Map<String, Long> stats) {
        stats.forEach((key, value) -> 
            redisTemplate.opsForValue().set(SEVERITY_STATS_KEY + ":" + key, 
                String.valueOf(value), CACHE_TTL));
    }
    
    /**
     * Get severity statistics
     */
    public Map<String, Long> getSeverityStats() {
        // This would need to scan keys - in production, use a different approach
        return Map.of();
    }
    
    /**
     * Invalidate cache for specific incident
     */
    public void invalidateIncident(String incidentId) {
        redisTemplate.delete(INCIDENT_SUMMARY_KEY + ":" + incidentId);
    }
    
    /**
     * Invalidate all dashboard caches
     */
    public void invalidateAll() {
        redisTemplate.delete(DASHBOARD_STATS_KEY);
        redisTemplate.delete(SEVERITY_STATS_KEY);
    }
    
    /**
     * Increment real-time signal counter
     */
    public long incrementSignalCounter() {
        return redisTemplate.opsForValue().increment("metrics:signals:count");
    }
    
    /**
     * Get signal counter value
     */
    public long getSignalCounter() {
        String value = redisTemplate.opsForValue().get("metrics:signals:count");
        return value != null ? Long.parseLong(value) : 0;
    }
    
    /**
     * Reset signal counter
     */
    public void resetSignalCounter() {
        redisTemplate.delete("metrics:signals:count");
    }
}