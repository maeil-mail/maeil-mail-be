# Architecture — 매일메일 Plus

## 스택
- Java 17, Spring Boot 3.5.3, Gradle 멀티모듈 (build-recipe-plugin)
- 신규 모듈: `learning-api` (독립 Spring Boot 앱, 포트 8081)
- DB: Supabase PostgreSQL (dev profile), H2 (test profile)
- 원본 DB: MySQL (prod), H2 (local/test) — learning-api에서는 직접 사용 안 함

## 원본 모듈 (절대 수정 금지)
- mail-core: Question, Subscribe, MailSender, 통계
- mail-api: 구독 REST API
- mail-admin: 관리자 기능
- mail-app: 실행 앱 (포트 8080)
- mail-batch: 메일 배치 발송
- wiki-api, wiki-core: Wiki 기능

## learning-api 내부 패키지
```
maeilmail.learning
├── LearningApiApplication.java
├── config/
│   ├── AsyncConfig.java          # mailExecutor ThreadPool
│   ├── MailConfig.java           # 프로파일별 LearningMailSender 빈
│   └── SchedulerConfig.java      # @EnableScheduling
├── common/
│   ├── ApiResponse.java          # 공통 응답 래퍼
│   ├── GlobalExceptionHandler.java
│   └── ErrorCode.java
├── domain/
│   ├── answer/
│   │   ├── Answer.java           # Entity
│   │   ├── AnswerRepository.java
│   │   ├── AnswerService.java
│   │   ├── dto/
│   │   └── event/AnswerSubmittedEvent.java
│   ├── wrongnote/
│   │   ├── WrongNote.java
│   │   ├── WrongNoteRepository.java
│   │   ├── WrongNoteService.java
│   │   ├── Sm2Algorithm.java     # SM-2 로직 분리
│   │   └── dto/
│   ├── userstat/
│   │   ├── UserStat.java         # @Version 낙관적 락
│   │   ├── UserStatRepository.java
│   │   ├── UserStatService.java  # synchronized 갱신
│   │   └── dto/
│   └── course/
│       ├── CourseEnrollment.java
│       ├── CourseEnrollmentRepository.java
│       ├── CourseService.java
│       ├── policy/               # Strategy 패턴
│       │   ├── CoursePolicy.java (interface)
│       │   ├── ShortIntensivePolicy.java
│       │   ├── HardOnlyPolicy.java
│       │   └── WeaknessFocusedPolicy.java
│       └── dto/
├── infrastructure/
│   ├── mail/
│   │   ├── LearningMailSender.java   # 인터페이스 (Adapter 패턴)
│   │   ├── SmtpMailSender.java       # Gmail SMTP 구현
│   │   └── MockMailSender.java       # local/test 구현
│   └── recommender/
│       ├── QuestionRecommender.java  # 인터페이스
│       ├── EasyRecommender.java
│       ├── MediumRecommender.java
│       ├── HardRecommender.java
│       └── QuestionRecommenderFactory.java  # Factory 패턴
├── adapter/
│   ├── LegacyQuestionPort.java       # 인터페이스
│   └── LegacyQuestionAdapter.java    # mail-core Question 연결
├── api/
│   ├── AnswerController.java
│   ├── WrongNoteController.java
│   ├── UserStatController.java
│   └── CourseController.java
└── event/
    └── listener/
        ├── WrongNoteRegistrationListener.java
        ├── UserStatUpdateListener.java
        └── MailNotificationListener.java
```

## 디자인 패턴 매핑

| 패턴 | 위치 | 목적 |
|---|---|---|
| Adapter | `infrastructure/mail/LearningMailSender` + 구현체들 | 환경별 메일 발송 방식 교체 |
| Strategy | `domain/course/policy/CoursePolicy` + 3개 구현 | 코스별 학습 문제 선택 로직 |
| Observer | `AnswerSubmittedEvent` + 3개 리스너 | 답안 제출 → 오답/통계/메일 처리 |
| Factory | `infrastructure/recommender/QuestionRecommenderFactory` | 난이도별 추천기 생성 |

## 동시성 / 비동기
- `ThreadPoolTaskExecutor("mailExecutor")` — 메일 발송 전용 스레드풀
- `@Async("mailExecutor")` + `@Scheduled` — 복습 메일 큐잉
- `AtomicInteger` / `synchronized` — UserStat 동시 갱신
- `@Version` — UserStat 낙관적 락
- Race Condition 시연 테스트: `UserStatRaceConditionTest`

## 레이어 책임
- Controller: HTTP 요청/응답 변환만. 비즈니스 로직 금지.
- Service: 트랜잭션 경계. 도메인 로직 조율.
- Repository: Spring Data JPA. 쿼리 정의.
- Event Listener: 비동기(@Async) 부수 효과 처리.

## 코드 컨벤션 (CONVENTIONS.md 우선)
- Lombok: @Getter, @RequiredArgsConstructor, @NoArgsConstructor
- 생성자 주입만 사용
- DTO: record 우선
- Optional: 반환 타입에만
