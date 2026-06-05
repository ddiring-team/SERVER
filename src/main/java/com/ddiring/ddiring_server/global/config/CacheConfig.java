package com.ddiring.ddiring_server.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    /** 어르신 주간 패턴 리포트 캐시. 키: WeeklyReportCacheKey(elderId, startDate, endDate) */
    public static final String WEEKLY_REPORT_CACHE = "weeklyReport";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(WEEKLY_REPORT_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(1)));
        return manager;
    }
}
