package com.ddiring.ddiring_server.domain.alert.domain.repository;

import com.ddiring.ddiring_server.domain.alert.domain.entity.RiskAlertHistory;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RiskAlertHistoryRepository extends JpaRepository<RiskAlertHistory, Long> {

    // 출석 미진행 알림: 같은 어르신에 대해 since 이후 알림이 있었는지
    @Query("""
            SELECT COUNT(h) > 0 FROM RiskAlertHistory h
            WHERE h.elder.id = :elderId
              AND h.alertType = :alertType
              AND h.createdAt >= :since
            """)
    boolean existsRecentByElderAndType(
            @Param("elderId") Long elderId,
            @Param("alertType") AlertType alertType,
            @Param("since") LocalDateTime since
    );

    // 주간 위험 알림: 같은 어르신 + 같은 카테고리 중복 방지
    @Query("""
            SELECT COUNT(h) > 0 FROM RiskAlertHistory h
            WHERE h.elder.id = :elderId
              AND h.alertType = :alertType
              AND h.category = :category
              AND h.createdAt >= :since
            """)
    boolean existsRecentByElderAndTypeAndCategory(
            @Param("elderId") Long elderId,
            @Param("alertType") AlertType alertType,
            @Param("category") String category,
            @Param("since") LocalDateTime since
    );
}
