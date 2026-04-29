# Ralph Loop Backlog

## 구현 순서 (위에서 아래로)

- [ ] local-bootstrap          — settings.gradle에 learning-api 추가, 모듈 골격, application-local.yml, bootRun 부팅 확인
- [ ] dev-profile-supabase     — application-dev.yml + .env 연동 + Supabase PostgreSQL 연결 검증
- [ ] domain-answer            — Answer 엔티티 + Repository + Service + 제출 API + 단위 테스트
- [ ] domain-wrongnote-sm2     — WrongNote + SM-2 알고리즘 + 복습 API + 단위 테스트 (경계값 포함)
- [ ] domain-userstat          — UserStat + 동시성 안전 갱신(synchronized + @Version) + Race Condition 시연 테스트
- [ ] event-observer           — AnswerSubmittedEvent + 3 리스너 (오답노트 / 통계 / 메일 큐잉) + ApplicationEventPublisher
- [ ] mail-strategy            — LearningMailSender 인터페이스 + Smtp/Mock 구현 + ThreadPoolTaskExecutor + Gmail 발송 통합 테스트
- [ ] course-policy-strategy   — CoursePolicy Strategy 3종 + CourseService + 오늘의 추천 API + 테스트
- [ ] recommender-factory      — QuestionRecommender 인터페이스 + 3 구현체 + QuestionRecommenderFactory
- [ ] adapter-original-domain  — LegacyQuestionPort 인터페이스 + LegacyQuestionAdapter (mail-core Question 연결)
- [ ] readme-and-final         — README 작성 + 캡처 자리 + 사용자 체크리스트
