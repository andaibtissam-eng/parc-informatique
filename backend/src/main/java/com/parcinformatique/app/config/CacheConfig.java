package com.parcinformatique.app.config;

import java.util.concurrent.ConcurrentMap;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(
            java.util.List.of(
                new ConcurrentMapCache("dashboard"),
                new ConcurrentMapCache("notifications"),
                new ConcurrentMapCache("reference-data")
            )
        );
        return manager;
    }
}
