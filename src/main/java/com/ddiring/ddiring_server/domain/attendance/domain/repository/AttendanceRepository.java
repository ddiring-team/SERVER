package com.ddiring.ddiring_server.domain.attendance.domain.repository;

import com.ddiring.ddiring_server.domain.attendance.domain.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUser_IdAndCheckedAt(Long userId, LocalDate checkedAt);

    @Query("SELECT a.checkedAt FROM Attendance a WHERE a.user.id = :userId AND a.checkedAt BETWEEN :start AND :end")
    List<LocalDate> findCheckedDatesByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user.id = :userId AND a.checkedAt BETWEEN :start AND :end")
    long countByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}