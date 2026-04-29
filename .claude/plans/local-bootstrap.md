# learning-api 모듈 골격 (local-bootstrap)

## 목표
settings.gradle에 learning-api 추가, 모듈 골격(build.gradle, LearningApiApplication, application-local.yml) 생성, bootRun으로 부팅 확인

## 영향 범위
- 새 파일:
  - settings.gradle (learning-api 추가 1줄)
  - learning-api/gradle.properties
  - learning-api/build.gradle
  - learning-api/src/main/java/maeilmail/learning/LearningApiApplication.java
  - learning-api/src/main/resources/application.yml
  - learning-api/src/main/resources/application-local.yml
  - learning-api/src/test/resources/application.yml
- 수정 파일:
  - settings.gradle (include 1줄 추가)
- 영향 X 모듈: mail-app, mail-api, mail-batch, wiki-api, mail-core, mail-admin, wiki-core

## 단계 (체크리스트)

- [ ] 1. settings.gradle에 `include 'learning-api'` 추가
- [ ] 2. learning-api/gradle.properties 생성 (type 설정)
- [ ] 3. learning-api/build.gradle 생성
  - Datadog 의존 제외 (루트 build.gradle이 application 타입에 micrometer-registry-datadog 자동 추가함)
  - PostgreSQL 드라이버 추가
  - Spring Boot Validation 추가
  - H2 (testRuntimeOnly)
  - Testcontainers PostgreSQL (testImplementation)
  - mail-core 의존 추가 (LegacyQuestionAdapter에서 필요)
- [ ] 4. 소스 디렉토리 구조 생성
- [ ] 5. LearningApiApplication.java 생성
- [ ] 6. application.yml (공통 설정: server.port=8081)
- [ ] 7. application-local.yml (H2 인메모리 DB, MockMailSender 활성)
- [ ] 8. 테스트용 application.yml (H2)
- [ ] 9. 컴파일 검증: ./gradlew :learning-api:compileJava

## 테스트 범위
- [ ] 단위 테스트: 없음 (골격만)
- [ ] 통합 테스트: 없음 (골격만)

## 디자인 패턴
해당 없음 (골격)

## 주의사항
- `type=java-boot-mvc-data-application` → 루트 build.gradle이 actuator + micrometer-datadog 자동 추가
  → learning-api/build.gradle에서 micrometer-datadog를 configurations.all exclude로 제거
- mail-core를 의존하면 QueryDSL APT 어노테이션 프로세서가 필요 → compileJava 전에 generateQuerydslSources 필요할 수 있음
  → 일단 mail-core 의존 없이 시작, adapter slug에서 추가
- local 프로파일: H2 + spring.jpa.hibernate.ddl-auto=create-drop
