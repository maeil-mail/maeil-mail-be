# API Spec — learning-api

모든 응답은 ApiResponse<T> 래핑:
```json
{ "success": true, "data": {...}, "error": null }
```
실패 시:
```json
{ "success": false, "data": null, "error": { "code": "ANSWER_NOT_FOUND", "message": "답안을 찾을 수 없습니다." } }
```

## 답안 제출
POST /api/answers
- Body: `{ "questionId": 1, "submittedText": "...", "responseTimeMs": 3000 }`
- 200: `{ "isCorrect": true, "score": 100, "correctAnswer": "...", "explanation": "..." }`
- 이벤트: AnswerSubmittedEvent 발행 → WrongNote 등록/삭제, UserStat 갱신, 메일 큐잉

## 오답노트
GET /api/wrong-notes/me?page=0&size=20
- 쿼리 파라미터 인증: ?email=xxx (임시, 실제 인증 미구현)
- 200: PagedApiResponse<WrongNoteDto>

GET /api/wrong-notes/me/due
- 오늘 복습 대상 (next_review_at <= now())
- 200: List<WrongNoteDto>

POST /api/wrong-notes/{id}/review
- Body: `{ "isCorrect": true }`
- SM-2 알고리즘으로 next_review_at, ease_factor, interval_days 갱신
- 200: UpdatedWrongNoteDto

## 통계
GET /api/stats/me?email=xxx
- 200: UserStatDto

## 코스
POST /api/courses/enroll
- Body: `{ "courseType": "SHORT_INTENSIVE" }`
- 200: CourseEnrollmentDto

GET /api/courses/me/today?email=xxx
- 오늘의 추천 문제 목록 (CoursePolicy Strategy 적용)
- 200: List<QuestionSummaryDto>

## 개발/테스트
POST /api/dev/test-mail
- Body: `{ "to": "test@example.com", "subject": "테스트" }`
- MockMailSender 동작 확인용 (local/test 프로파일에서만 활성)
- 200: `{ "sent": true, "log": "..." }`

GET /actuator/health
- 200: `{ "status": "UP" }`
