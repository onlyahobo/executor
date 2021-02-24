package com.example.m11skowr.executor.module.cache;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static com.example.m11skowr.executor.module.cache.Cache.CACHE1;
import static com.example.m11skowr.executor.module.cache.Cache.CACHE2;

@Configuration
class CacheConfiguration {

    @Bean
    public RedisCacheManagerBuilderCustomizer myRedisCacheManagerBuilderCustomizer() {
        return builder -> builder.withCacheConfiguration(CACHE1, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)))
            .withCacheConfiguration(CACHE2, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)));
    }

}
