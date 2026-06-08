# 띠링(Ddiring) — Backend

> 어르신과 가족을 잇는 **AI 기반 일상 돌봄 연결 서비스**의 Spring Boot 백엔드 서버

<p>
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-AWS%20RDS-4479A1?logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/AWS-S3%20%7C%20EC2%20%7C%20ECR-FF9900?logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/Docker-CI%2FCD-2496ED?logo=docker&logoColor=white">
</p>

> '띠링'은 알림음을 연상시키는 이름으로, 어르신의 안부가 알림처럼 가족에게 따뜻하게 전해진다는 의미를 담았습니다.

---

## 📌 프로젝트 소개

독거노인이 증가하면서 보호자 가족이 멀리서 어르신의 일상 안부를 매일 확인하기 어려운 상황이 늘고 있습니다. 기존의 안부 확인 방식(전화, 활동 감지 센서 등)은 어르신의 **정서·건강 상태를 구조적으로 누적·관찰하기 어렵다**는 한계가 있습니다.

**띠링**은 어르신의 일상(약 복용·식사·기분·활동·안전 등)을 매일 **체크리스트(설문)** 형태로 가볍게 확인하고, 그 결과를 보호자가 한눈에 모니터링할 수 있게 합니다.

- **개인화된 안부 경험** — AI가 어르신 프로필·최근 응답을 반영해 반복되는 기계적 질문을 따뜻한 발화로 변환하고 TTS 음성까지 제공
- **데이터 기반 돌봄** — 일일 응답을 누적·집계해 일일 요약·주간 패턴 리포트를 생성하고, 위험 신호(연속 약 복용 누락, 우울감 지속 등)를 감지해 보호자에게 알림 발송
- **가족 간 정서적 연결** — 일일 사진 공유, 가족방(초대코드), 출석 체크로 단순 모니터링을 넘어선 정서적 교류 채널 제공

> 본 레포지토리는 **백엔드(Spring Boot)** 서버입니다. 프론트엔드(React Native)·AI(FastAPI) 서버는 별도 레포지토리로 운영됩니다.

---

## ✨ 주요 기능

| 도메인 | 설명 |
|---|---|
| **인증 (auth)** | Kakao OAuth2 소셜 로그인 + JWT Stateless 인증, 보호자/어르신 회원가입·로그인 |
| **사용자 (user)** | 보호자(`GUARDIAN`)·어르신(`ELDER`) 역할 관리, 어르신 프로필, FCM 토큰 등록 |
| **가족방 (family)** | 6자리 초대코드 기반 가족방 생성·참여, 구성원 승인/관리 |
| **설문 (survey)** | 카테고리별 체크리스트 설문 생성·관리, AI 질문 변환·TTS, 응답 제출, 일일 요약·주간 리포트 |
| **출석 (attendance)** | 어르신 출석 체크, 월간 출석 조회, 미출석 리마인더 |
| **사진 (photo)** | 일일 사진 업로드·피드, 이모지 반응 |
| **온도 (temperature)** | 활동 기반 가족 관계 '온도' 점수 집계, 주간 추이 조회 |
| **알림 (alert)** | 알림 설정 관리, 위험 신호 자동 감지 및 푸시(FCM) 발송, 스케줄러 기반 모니터링 |

### AI 서버(FastAPI) 연동

백엔드는 어르신 프로필·최근 응답을 모아 AI 서버에 **Push 방식**으로 호출하고, 그 결과(변환 질문·요약·리포트·위험 문구)를 받아 저장·전달하는 **AI 연동 허브** 역할을 합니다. 비결정적 AI 응답 특성을 보완하기 위해 **Spring Cache(Caffeine)** 로 변환 결과·주간 리포트를 캐싱합니다.

설문 카테고리는 약 복용·건강 상태·기분/감정·식사/수분·활동/외출·안전/생활·인지 상태·낙상 예방·가족 소통의 9종으로 구성됩니다.

---

## 🛠 기술 스택

| 구분 | 기술 |
|---|---|
| **언어 / 런타임** | Java 17 |
| **프레임워크** | Spring Boot 3.5 (Web, Data JPA, Security, Cache) |
| **인증** | Spring Security, JWT (jjwt 0.11.5, HS512), Kakao OAuth2 Client |
| **데이터베이스** | MySQL (AWS RDS) · H2 (테스트) |
| **캐시** | Spring Cache + Caffeine |
| **스토리지** | AWS S3 (Presigned URL 직접 업로드) |
| **푸시 알림** | Firebase Admin SDK (FCM) |
| **API 문서** | springdoc-openapi (Swagger UI) 2.8 |
| **빌드 / 배포** | Gradle, Docker / Docker Compose, AWS EC2·ECR, GitHub Actions |

---

## 🏗 시스템 아키텍처



<img width="1690" height="931" alt="image" src="https://github.com/user-attachments/assets/b9d6a16f-df3e-40a5-8ca3-4bb5a28309ac" />


---

## 📁 프로젝트 구조

```
com.ddiring.ddiring_server
├── domain/                      # 비즈니스 도메인 (기능별 패키지)
│   ├── auth/                    # 인증 (Kakao OAuth, 로그인/회원가입)
│   ├── user/                    # 사용자, 어르신 프로필
│   ├── family/                  # 가족방, 구성원
│   ├── survey/                  # 설문·세션·질문·응답, AI 캐시
│   ├── attendance/              # 출석
│   ├── photo/                   # 일일 사진, 이모지 반응
│   ├── temperature/             # 관계 온도 점수
│   └── alert/                   # 알림 설정, 위험 감지 스케줄러
│       └── application/scheduler/
└── global/                      # 공통 인프라
    ├── config/                  # Security, S3, Swagger, Cache, Firebase, Scheduling
    ├── security/                # JWT 필터, TokenProvider
    ├── storage/                 # S3 서비스 및 컨트롤러
    ├── client/fastapi/          # AI 서버(FastAPI) 연동 클라이언트
    ├── notification/            # FCM 발송 서비스
    ├── common/                  # ApiResponse 래퍼, ErrorCode, 예외 처리
    └── entity/                  # BaseEntity (createdAt, updatedAt)
```

각 도메인 내부는 `domain/entity` · `domain/repository` · `application/service` · `presentation` 계층으로 구성합니다.

### 아키텍처 규칙

- **엔티티** — 모든 엔티티는 `BaseEntity`를 상속해 `createdAt`/`updatedAt` 자동 관리. `@NoArgsConstructor(PROTECTED)` + `@Builder` 패턴. PK 컬럼명은 `{엔티티명}_id`.
- **응답 형식** — 모든 API는 `ApiResponse<T>` 레코드(`success`, `status`, `message`, `data`)로 감싸 반환.
- **에러 처리** — `ErrorCode` enum에 HTTP 상태코드·메시지를 정의하고 `CustomException`으로 throw, `GlobalExceptionHandler`에서 일괄 처리.
- **보안** — JWT HS512(만료 1일), `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter` 순서로 체인 구성.

---

## 🌐 API 개요

전체 API 명세는 실행 후 Swagger UI에서 확인할 수 있습니다.

- **로컬**: `http://localhost:8080/swagger-ui/index.html`
- **운영**: `https://api.ddiringapp.com/swagger-ui/index.html`

| 리소스 | Base Path | 주요 엔드포인트 |
|---|---|---|
| 인증 | `/api/auth` | 보호자/어르신 회원가입·로그인, Kakao 로그인 완료, 로그아웃 |
| 사용자 | `/api/users` | `GET /me`, `PUT /fcm-token` |
| 가족방 | `/api/families` | 가족방 생성·참여, 초대코드 발급, 구성원 승인/삭제 |
| 설문 | `/api/surveys` | 설문 생성·조회·활성화·삭제, `GET /today` |
| 설문 세션 | `/api/survey-sessions` | 세션 생성·응답 제출, 주간 리포트, 어르신별 오늘 현황·리마인더 |
| 출석 | `/api/attendances` | 출석 체크, 오늘/월간 조회, 리마인더 |
| 사진 | `/api/photos` | 사진 업로드·피드 조회, 이모지 반응 |
| 온도 | `/api/temperatures` | 내/어르신 주간 온도 조회 |
| 알림 설정 | `/api/users/me/notification-settings` | 알림 설정 조회·수정 |
| S3 | `/api/s3` | `POST /presigned-url` |

**Public 경로**: `/`, `/api/s3/presigned-url`, `/api/users`, `/swagger-ui/**`, `/v3/api-docs/**`

---

## ⚙️ 프로파일

| 프로파일 | 용도 | DB |
|---|---|---|
| `local` | 로컬 개발 (기본값) | `localhost:3306/ddiring` |
| `dev` | 개발 서버 | AWS RDS |
| `test` | 단위/통합 테스트 | H2 in-memory |

---

## 🥇 Database

<img width="2264" height="1708" alt="image" src="https://github.com/user-attachments/assets/5d698b47-712e-4a6f-a3da-e43697f15e52" />


## 👥 팀

**캡스톤디자인 07분반 5조 — 띠링(Ddiring)**

| 이름 | 역할 |
|---|---|
| 김현수 | Frontend (React Native) |
| 박성제 | Backend (Spring Boot) |
| 이현택 | AI (FastAPI / LangChain) |

- **GitHub Organization**: https://github.com/ddiring-team
- **API 명세서 (Swagger)**: https://api.ddiringapp.com/swagger-ui/index.html
