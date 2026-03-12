package com.ddiring.ddiring_server.global.security;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
// jwt 토큰을 생성하고 , 검증한다.
// 사용자가 로그인성공하면, 해당 사용자에 대한 jwt 토큰을 생성해서 클라이언트에게 전달
// jwt 토큰 검증 및 사용자 id 추출. 이후, 클라가 api 요청시 전달한 토큰 검증, 그 안에 담긴 사용자 id 를 추출해 인증에 활용
public class TokenProvider {

    // 환경변수에서 JWT 서명키를 가져옴
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    // 비밀키로부터 HMAC SHA 키 객체 생성 (secret key 를 바탕으로 만들어진 서명용 key 객체)
    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 사용자 정보 기반으로 jwt 토큰 생성
    public String create(User userEntity) {
        // 토큰 만료 기간을 현재 시각으로부터 1일 뒤로 설정
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        // jwt 생성 및 반환
        return Jwts.builder()
                .signWith(signingKey, SignatureAlgorithm.HS512) // 서명 알고리즘 및 키 설정
                .setSubject(String.valueOf(userEntity.getId())) // 사용자 id 를 subject 로 설정
                .setIssuer("demo app") // 토큰 발급자 정보 설정
                .setIssuedAt(new Date()) // 토큰 발급 시간 설정
                .setExpiration(expiryDate) // 만료 시간 설정
                .compact(); // 토큰 생성 완료
    }

    // 토큰 검증하고, 포함된 사용자 id 를 반환
    public String validateAndGetUserId(String token) {
        // 토큰 파싱 및 검증 (서명 유효 확인)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();// payload(claims) 추출

        return claims.getSubject(); // 사용자 id 반환
    }


    // 사용자 id 기반으로 토큰 생성 (예 : OAuth 사용자의 경우) 위 create 메소드와 다른점은, userId 만 받는다는 점
    // 주로 로그인 이후, 사용자 정보를 간단히 전달할때 사용하기 좋다.
    public String createByUserId(final Long userId) {
        // 만료일 1일 후로 설정
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        // 토큰 생성
        return Jwts.builder()
                .signWith(signingKey, SignatureAlgorithm.HS512) // 서명 알고리즘 및 키 설정
                .setSubject(String.valueOf(userId)) // 사용자 id 를 subject 로 설정
                .setIssuedAt(new Date()) // 토큰 발급 시간 설정
                .setExpiration(expiryDate) // 만료 시간 설정
                .compact(); // 토큰 생성 완료
    }

}
