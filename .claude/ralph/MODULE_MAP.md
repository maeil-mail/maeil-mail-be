# Module Map — 매일메일 서버

분석일: 2026-04-30

## 모듈 목록

| 모듈 | type (gradle.properties) | 역할 | 주요 의존성 |
|---|---|---|---|
| mail-core | java-boot-data-mail-lib | 핵심 도메인 (Question, Subscribe, MailSender, 통계) | Spring Data JPA, QueryDSL, MySQL, Spring Mail, Bucket4j, spring-retry |
| mail-api | java-boot-mvc-lib | 구독/질문 REST API 레이어 | mail-core |
| mail-admin | (boot 미적용) | 관리자 기능 | mail-core |
| mail-app | java-boot-mvc-application | 실행 가능 앱 (Spring Boot Main) | mail-api, wiki-api, mail-admin, Datadog |
| mail-batch | java-boot-batch-application | 배치 작업 (메일 발송 스케줄) | mail-core |
| wiki-api | java-boot-mvc-lib | Wiki REST API | wiki-core, validation |
| wiki-core | java-boot-lib | Wiki 도메인 (JWT 인증) | jjwt, validation |

## 패키지 구조 (감지 결과)

- 베이스 패키지: `maeilmail`
- 도메인 패키지 패턴: `maeilmail.{domain}.command.domain` / `maeilmail.{domain}.command.application`
- 쿼리 패키지: `maeilmail.{domain}.query`
- config: `maeilmail.config`

## 신규 모듈 결정

- 이름: `learning-api`
- type: `java-boot-mvc-data-application` (독립 실행 가능, JPA, REST API)
- 베이스 패키지: `maeilmail.learning`
- Spring Boot Main: `maeilmail.learning.LearningApiApplication`
- 포트: 8081 (mail-app 이 8080 사용 추정)

## 주요 클래스 참조

- `maeilmail.question.Question` — 원본 Question 엔티티 (mail-core)
- `maeilmail.question.QuestionCategory` — FRONTEND / BACKEND
- `maeilmail.subscribe.command.domain.Subscribe` — 구독 엔티티
- `maeilmail.mail.MailSender` — 원본 메일 발송 클래스 (@Component("emailSender"))
  - **주의**: learning-api 의 메일 인터페이스는 `LearningMailSender` 로 명명 (이름 충돌 방지)
- `maeilmail.BaseEntity` — createdAt/updatedAt JPA Auditing

## 기존 모듈 침범 금지

- mail-core, mail-admin, mail-api, mail-app, mail-batch, wiki-api, wiki-core
- 위 모듈의 소스 파일 수정/추가 절대 금지
