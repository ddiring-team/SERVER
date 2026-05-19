package com.ddiring.ddiring_server.domain.photo.domain.entity;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
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
@Table(name = "daily_photo")
public class DailyPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;              // 소속 가족방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 업로드한 유저

    @Column(nullable = false, length = 500)
    private String photoUrl;            // S3 이미지 URL

    @Column(nullable = false, length = 200)
    private String caption;             // 사진 설명 텍스트

    @Column(nullable = false)
    private LocalDate takenDate;        // 캘린더 기준 날짜
}
