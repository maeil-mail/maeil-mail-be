# 비즈니스 규칙

## SM-2 복습 알고리즘 (단순화)

### 정답 시
```
new_interval = max(1, round(interval_days * ease_factor))
new_ease_factor = ease_factor + 0.1
next_review_at = now() + new_interval days
```

### 오답 시
```
new_interval = 1  (리셋)
new_ease_factor = max(1.3, ease_factor - 0.2)
next_review_at = now() + 1 day
```

### 초기값
- interval_days = 1
- ease_factor = 2.5
- review_count = 0

---

## 난이도 자동 조정

최근 20문제 정답률 기준:
- > 0.8 → 한 단계 상승 (EASY→MEDIUM, MEDIUM→HARD, HARD 유지)
- < 0.4 → 한 단계 하강 (HARD→MEDIUM, MEDIUM→EASY, EASY 유지)
- 그 외 → 유지

---

## 코스 정책 (Strategy)

### SHORT_INTENSIVE (7일 집중)
- 7일간 매일 5문제
- 핵심 카테고리 (BACKEND, FRONTEND) 번갈아
- 문제 중복 없도록 sequence 관리

### HARD_ONLY
- HARD 난이도만
- 하루 문제 수 제한 없음
- 사용자 정답률 하위 문제 우선

### WEAKNESS_FOCUSED
- 사용자 카테고리별 정답률 하위 30% 문제에서 선택
- 없으면 전체 문제에서 랜덤

---

## 메일 발송 규칙

### 발송 시간
- 매일 06:00 KST (`@Scheduled cron = "0 0 6 * * *"`)
- 복습 대상 (next_review_at <= 오늘) 있는 사용자에게만

### 구현체 선택 (Adapter 패턴)
| profile | 구현체 | 동작 |
|---|---|---|
| local | MockMailSender | 콘솔 로그 출력 |
| dev | SmtpMailSender | Gmail SMTP 실제 발송 (본인→본인) |
| test | MockMailSender | 인메모리 기록 (테스트 검증용) |
| prod | 자리만 마련 | 빈 미등록 (운영 도입 시 결정) |

### 스레드풀
- ThreadPoolTaskExecutor bean name: "mailExecutor"
- corePoolSize: 3, 학습 환경이므로 소규모

---

## 동시성 규칙

### UserStat 갱신
- Service 메서드에 `synchronized` 적용 (단일 JVM 환경 전제)
- 엔티티에 `@Version` 필드 추가 (낙관적 락, 충돌 시 OptimisticLockException)

### Race Condition 시연 테스트
- 100 스레드 × 1000 증가 = 100,000
- UnsafeCounter: synchronized 없음 → 손실 발생 (isLessThan 100,000)
- SafeCounter: synchronized or AtomicLong → 손실 없음 (isEqualTo 100,000)
- 콘솔 출력 형식: `[UNSAFE] expected=100000 actual={n}` / `[SAFE] expected=100000 actual=100000`

---

## 엔티티 제약

### Answer
- user_id: email 문자열 (임시, 실제 회원 테이블 미구현)
- question_id: mail-core의 Question.id 참조 (FK 아님, Long 값만 보관)
- is_correct: boolean
- submitted_text: TEXT
- score: int (0~100)
- response_time_ms: long
- created_at

### WrongNote
- unique(user_email, question_id)
- next_review_at 인덱스 필수
- ease_factor: double (2.5 ~ 제한 없음, 최소 1.3)
- interval_days: int (최소 1)

### UserStat
- unique per user_email
- current_difficulty: enum EASY/MEDIUM/HARD
- version: Long (@Version, 낙관적 락)

### CourseEnrollment
- 동시에 1개 활성 코스만 (ended_at is null)
