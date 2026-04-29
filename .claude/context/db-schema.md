# DB Schema — learning-api (Supabase PostgreSQL)

## answers
```sql
CREATE TABLE answers (
    id              BIGSERIAL PRIMARY KEY,
    user_email      VARCHAR(255) NOT NULL,
    question_id     BIGINT NOT NULL,
    submitted_text  TEXT NOT NULL,
    is_correct      BOOLEAN NOT NULL,
    score           INT NOT NULL DEFAULT 0,
    response_time_ms BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_answers_user_created ON answers (user_email, created_at DESC);
```

## wrong_notes
```sql
CREATE TABLE wrong_notes (
    id              BIGSERIAL PRIMARY KEY,
    user_email      VARCHAR(255) NOT NULL,
    question_id     BIGINT NOT NULL,
    last_reviewed_at TIMESTAMP,
    next_review_at  TIMESTAMP NOT NULL,
    review_count    INT NOT NULL DEFAULT 0,
    ease_factor     DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days   INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_wrong_notes_user_question UNIQUE (user_email, question_id)
);
CREATE INDEX idx_wrong_notes_next_review ON wrong_notes (next_review_at);
CREATE INDEX idx_wrong_notes_user ON wrong_notes (user_email);
```

## user_stats
```sql
CREATE TABLE user_stats (
    id                  BIGSERIAL PRIMARY KEY,
    user_email          VARCHAR(255) NOT NULL UNIQUE,
    total_attempts      INT NOT NULL DEFAULT 0,
    correct_count       INT NOT NULL DEFAULT 0,
    current_difficulty  VARCHAR(10) NOT NULL DEFAULT 'EASY',
    avg_response_time_ms BIGINT NOT NULL DEFAULT 0,
    last_active_at      TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    version             BIGINT NOT NULL DEFAULT 0
);
```

## course_enrollments
```sql
CREATE TABLE course_enrollments (
    id          BIGSERIAL PRIMARY KEY,
    user_email  VARCHAR(255) NOT NULL,
    course_type VARCHAR(30) NOT NULL,
    started_at  TIMESTAMP NOT NULL DEFAULT now(),
    ended_at    TIMESTAMP
);
CREATE INDEX idx_course_enrollments_user ON course_enrollments (user_email);
```

## 참고
- JPA ddl-auto: `update` (dev), `create-drop` (test), `validate` (prod)
- 원본 Question 테이블은 mail-core MySQL DB에 존재. learning-api에서는 question_id(Long)만 저장.
