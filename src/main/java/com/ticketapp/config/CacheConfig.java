package com.ticketapp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable caching in the application.
 * Uses ConcurrentMapCacheManager for simplicity and Spring Boot 3.x compatibility.
 * In production, this can be replaced with EhCache or Redis for better performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Use ConcurrentMapCacheManager for simplicity
        // This provides in-memory caching with automatic cache creation
        return new ConcurrentMapCacheManager("tickets", "userTickets");
    }
}
