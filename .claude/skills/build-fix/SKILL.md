---
name: build-fix
description: "컴파일/빌드 에러 즉시 수정."
---

# build-fix
1. ./gradlew :learning-api:compileJava --quiet 2>&1 → 에러 수집
2. 첫 에러 파일 Read
3. 에러 직접 관련 줄만 수정 (정밀 수정 원칙)
4. 재컴파일
5. 같은 에러 3회 → 중단 후 보고
