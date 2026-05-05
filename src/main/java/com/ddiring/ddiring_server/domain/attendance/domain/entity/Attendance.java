package com.ddiring.ddiring_server.domain.attendance.domain.entity;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "checked_at"}))
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 출석한 어르신

    @Column(nullable = false)
    private LocalDate checkedAt;        // 출석 날짜 (중복 방지 기준)

}
