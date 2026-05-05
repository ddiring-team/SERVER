# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**띠링(Ddiring)** 서버 — 어르신과 보호자 가족을 연결하는 서비스의 Spring Boot 백엔드.

- Java 17, Spring Boot 3.5, Spring Security (JWT stateless), Spring Data JPA (MySQL)
- AWS S3 (이미지 업로드), Swagger(springdoc-openapi 2.8), H2 (테스트 전용)

## 주요 명령어

```bash
# 빌드
./gradlew build

# 테스트 전체 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.ddiring.ddiring_server.global.storage.presentation.S3ControllerTest"

# 로컬 실행 (application-local.yml 활성화)
./gradlew bootRun

# Docker 이미지 빌드 (빌드 후)
docker build -t ddiring-server .
```

## 프로파일 설정

| 프로파일 | 용도 | DB |
|---|---|---|
| `local` | 로컬 개발 (기본값) | `localhost:3306/ddiring` |
| `dev` | 개발 서버 | AWS RDS |
| `test` | 단위/통합 테스트 | H2 in-memory |

로컬 실행 시 환경변수 필요: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_S3_BUCKET`, `JWT_SECRET_KEY`

## 패키지 구조

```
com.ddiring.ddiring_server
├── domain/                  # 비즈니스 도메인 (기능별 패키지)
│   ├── user/
│   ├── family/
│   ├── attendance/
│   ├── photo/
│   └── survey/
└── global/                  # 공통 인프라
    ├── config/              # Spring 설정 (Security, S3, Swagger)
    ├── security/            # JWT 필터, TokenProvider
    ├── storage/             # S3 서비스 및 컨트롤러
    ├── common/
    │   ├── response/        # ApiResponse<T> 공통 응답 래퍼
    │   └── exception/       # ErrorCode enum, GlobalExceptionHandler
    └── entity/              # BaseEntity (createdAt, updatedAt)
```

각 도메인 내부는 `domain/entity/`, `domain/repository/`, `application/service/`, `presentation/` 계층으로 구성한다.

## 아키텍처 핵심 규칙

**엔티티 설계**
- 모든 엔티티는 `BaseEntity`를 상속해 `createdAt`/`updatedAt` 자동 관리
- 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder` 패턴 사용
- PK 컬럼명은 `{엔티티명}_id` (예: `user_id`)

**응답 형식**
- 모든 API는 `ApiResponse<T>` 레코드로 감싸서 반환 (`success`, `status`, `message`, `data`)
- 에러는 `ErrorCode` enum에 HTTP 상태코드와 메시지를 함께 정의하고 `CustomException`으로 throw

**보안**
- JWT는 HS512, 만료 1일. `TokenProvider`가 생성/검증 담당
- `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter` 순서로 체인 구성
- Public 경로: `/`, `/api/s3/presigned-url`, `api/users`, `/swagger-ui/**`, `/v3/api-docs/**`

**S3 업로드 방식**
- 클라이언트가 Presigned URL을 발급받아 직접 S3에 PUT 업로드 (유효 10분)
- 허용 MIME 타입은 `AllowedMimeType` enum으로 관리

## 도메인 모델 관계

- `User`: 보호자(`GUARDIAN`) / 어르신(`ELDER`) 역할 구분, 전화번호로 식별
- `ElderProfile`: 어르신 추가 프로필 (User와 1:1)
- `Family`: 초대코드(6자리) 기반 가족방, `createdBy`(User)가 주 보호자
- `FamilyMember`: User ↔ Family 다대다 연결 (`MemberStatus` enum 포함)
- `Survey` / `SurveySession` / `SurveyQuestion` / `SurveyQuestionOption` / `SurveyAnswer`: 설문 계층 구조
- `Attendance`: 출석 체크
- `DailyPhoto`: 일일 사진 (Family 소속)
