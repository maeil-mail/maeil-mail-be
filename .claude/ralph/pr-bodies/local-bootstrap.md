## 무엇을
learning-api 독립 Spring Boot 모듈 골격 추가 (포트 8081, local/H2 프로파일 부팅 확인)

## 왜
매일메일 Plus 학습 기능(답안 제출, 오답노트 SM-2, 코스 정책, 비동기 메일 발송)을 원본 서비스에 영향 없이 개발하기 위해 독립 모듈이 필요하다. 기존 mail-app(포트 8080)과 공존하며, 학습 기능만의 DB 스키마(Supabase PostgreSQL)를 운영한다.

## 어떻게
- `settings.gradle`에 `include 'learning-api'` 추가
- `gradle.properties`: `type=java-boot-mvc-application` (build-recipe-plugin 연동)
- `build.gradle`: actuator 자동 추가 유지, Datadog 제외, JPA + PostgreSQL + H2 추가
- `LearningApiApplication`: `@EnableJpaAuditing`, `@EnableAsync`, `@EnableScheduling` 일괄 선언
- `application-local.yml`: H2 인메모리, create-drop, MockMailSender 활성 플래그

## 고려했으나 채택하지 않은 대안

- **대안**: `type=java-boot-mvc-data-application` (기존 data 타입 그대로 사용)
  - **장점**: 루트 build.gradle의 data 블록(JPA, QueryDSL, MySQL)을 그대로 상속
  - **기각 이유**: QueryDSL APT와 MySQL 드라이버가 불필요하게 포함됨. learning-api는 PostgreSQL(Supabase)을 쓰고 QueryDSL을 사용하지 않음. 불필요한 의존을 배제하는 것이 빌드 속도와 명확성에 유리.

- **대안**: 기존 mail-app에 learning 패키지를 추가
  - **장점**: 모듈 추가 없이 빠르게 시작 가능
  - **기각 이유**: mail-app은 MySQL + Datadog 등 운영 의존이 강하게 묶여 있어, 학습 기능의 PostgreSQL/독립 스케줄러 설정이 충돌. 기존 운영 코드에 부작용 없이 개발하려면 모듈 분리가 필수.

## 의도적으로 구현하지 않은 것

- Spring Security / JWT 인증
  - **이유**: 1일 과제 범위에서 API 시연이 우선. 인증 없이 email 파라미터로 사용자 식별(개발 편의).
  - **운영 시 검토 대상**: wiki-core의 JWT 구현 재사용 또는 Spring Security 도입.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| 독립 모듈 분리 | 기존 코드 무영향, 독립 DB/설정 | 모듈 간 공통 코드 공유 불편 |
| `type=java-boot-mvc-application` | QueryDSL/MySQL 불필요 의존 없음 | data 블록 혜택(JPA 자동설정) 직접 작성 필요 |

## 변경 사항
- 추가: `learning-api/` 모듈 전체 골격 (build.gradle, gradle.properties, LearningApiApplication, application.yml, application-local.yml)
- 수정: `settings.gradle` — `include 'learning-api'` 추가
