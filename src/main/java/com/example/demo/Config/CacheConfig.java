package com.example.demo.Config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        cacheManager.setCacheNames(java.util.Arrays.asList(
                "coordinates",
                "validations",
                "distances"
        ));

        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }
}