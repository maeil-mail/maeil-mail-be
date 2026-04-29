# 커밋 컨벤션 (git log 분석 결과)

## prefix
feat / fix / refactor / chore / docs / test

## scope
사용 안 함 (기존 커밋에서 scope 패턴 미발견)

## 언어
한국어 (메시지 본문)

## 형식
```
{prefix}: {한국어 요약}
```

## 예시 (실제 커밋 5개)
- `feat: subscribe 생성자 추가`
- `fix: gradle 정리`
- `fix: 미사용 클래스 제거`
- `refactor: 콜백 메서드 분리`
- `chore: 임포트문 최적화`

## body
본문 없이 한 줄 요약만 사용 (분석된 커밋 대부분 본문 없음)

---

# PR 컨벤션

## 제목 형식
커밋 메시지와 동일: `{prefix}: {한국어 요약}`

## PR 본문
- GitHub PR 템플릿 파일 없음 (감지 실패)
- .claude/ralph/templates/pr-body.md 템플릿 사용

## 머지 전략
- CI: ./gradlew clean build (테스트 포함)
- 자동 머지 없음 (사용자가 직접 결정)

---

# 코드 스타일 (기존 코드 분석 결과)

## 네이밍
- 클래스: PascalCase
- 메서드/변수: camelCase
- 상수: UPPER_SNAKE_CASE
- 패키지: 소문자 dot-separated

## Lombok
- `@Getter`, `@RequiredArgsConstructor`, `@NoArgsConstructor(access = PROTECTED)`, `@AllArgsConstructor`
- 빌더는 사용 자제 (기존 코드에서 드물게 사용)

## Spring
- 생성자 주입만 사용 (`@RequiredArgsConstructor` + `final` 필드)
- 필드 주입(`@Autowired`) 금지

## DTO
- record 우선 (Spring Boot 3.x)

## 트랜잭션
- Service 클래스에 `@Transactional` / `@Transactional(readOnly = true)`

## 패키지 구조 (도메인 내부)
- `command.domain` — 엔티티, 리포지토리
- `command.application` — 서비스, 요청/응답 DTO
- `query` — 조회 전용 서비스

## 들여쓰기
- 4 spaces (공백)

## 기타
- Optional: 반환 타입에만 사용
- 스트림/람다: 적극 활용 (기존 코드 패턴)
