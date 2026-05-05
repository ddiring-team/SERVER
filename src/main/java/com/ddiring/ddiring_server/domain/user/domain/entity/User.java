package com.ddiring.ddiring_server.domain.user.domain.entity;

import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;                // 유저 이름

    @Column(unique = true, length = 20)
    private String phone;               // 전화번호 (로그인 식별자)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                  // 보호자 / 어르신

    private LocalDate birthDate;        // 생년월일


}
