# Ralph Loop 자율 결정 기록

## D-001 (2026-04-30) — learning-api 메일 인터페이스 이름
**결정**: `LearningMailSender` (인터페이스명)
**이유**: 원본 mail-core에 `maeilmail.mail.MailSender`(@Component("emailSender"))가 이미 존재.
learning-api 는 독립 Spring context 이므로 빈 이름 충돌 없지만, 코드 가독성을 위해 구분.

## D-002 (2026-04-30) — learning-api 베이스 패키지
**결정**: `maeilmail.learning`
**이유**: 원본 전체가 `maeilmail` 베이스 사용. 하위 패키지로 자연스럽게 편입.

## D-003 (2026-04-30) — learning-api gradle type
**결정**: `type=java-boot-mvc-data-application`
**이유**: 독립 실행 가능(application), REST API(mvc), JPA(data) 모두 필요.
Datadog(micrometer-registry-datadog)는 포함하지 않음 — actuator만 필요, DD 없이도 health 엔드포인트 제공.
→ 루트 build.gradle의 `configureByTypeHaving(['boot','mvc','application'])` 블록이 `actuator + datadog` 의존을 추가하므로
  **learning-api는 독자 build.gradle에서 DD를 exclusion 처리**한다.

## D-004 (2026-04-30) — 원본 모듈 연결 방식
**결정**: learning-api는 mail-core를 의존하지 않고 LegacyQuestionPort 인터페이스로 추상화
**이유**: mail-core 의 QueryDSL/MySQL/Bucket4j 등 무거운 의존이 learning-api에 전파되는 것을 방지.
개발/테스트 환경에서 learning-api만 독립 실행 가능하도록 유지.
단, 실제 구현체(LegacyQuestionAdapter)는 mail-core에 있는 QuestionRepository를 직접 호출.
→ 빌드 시 learning-api build.gradle에 `implementation project(':mail-core')` 추가 (의존 주입 목적).

## D-005 (2026-04-30) — DB 방언
**결정**: learning-api는 PostgreSQL(Supabase) 사용
**이유**: .env에 Supabase PostgreSQL URL 이미 제공됨.
원본은 MySQL 사용이나 learning-api는 별도 DB 스키마.
→ 하이버네이트 방언: `org.hibernate.dialect.PostgreSQLDialect`
→ 드라이버: `org.postgresql:postgresql`

## D-006 (2026-04-30) — UserStat 동시성 전략
**결정**: `synchronized` + `@Version`(낙관적 락) 이중 전략 모두 구현, 단위 테스트로 비교 시연
**이유**: 학습 목표가 동시성 패턴 이해이므로 두 방식 모두 코드로 보여주는 게 가치 있음.

## D-007 (2026-04-30) — learning-api 포트
**결정**: 8081
**이유**: mail-app이 기본 8080 사용 추정. 충돌 방지.
