package com.ddiring.ddiring_server.domain.survey.application.cache;

import java.time.LocalDate;

/**
 * 주간 리포트 캐시 키. record라 equals/hashCode가 자동 생성되어 캐시 맵 키로 안전하게 사용된다.
 */
public record WeeklyReportCacheKey(Long elderId, LocalDate startDate, LocalDate endDate) {
}
