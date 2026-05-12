package com.ddiring.ddiring_server.domain.user.domain.repository;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByPhone(String phone);

    Optional<User> findByKakaoId(String kakaoId);

    boolean existsByKakaoId(String kakaoId);
}
