#!/bin/bash
INPUT=$(cat)
FILE=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty' 2>/dev/null)
[[ -z "$FILE" ]] && exit 0
[[ "$FILE" != *.java ]] && exit 0
[[ "$FILE" != *learning-api* ]] && exit 0

PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo "/Applications/Github/java-midterm-project")
echo "[$FILE] learning-api 컴파일 검증 중..." >&2
cd "$PROJECT_ROOT" && ./gradlew :learning-api:compileJava --quiet 2>&1
if [[ $? -eq 0 ]]; then
  echo "컴파일 통과" >&2
  exit 0
else
  echo "컴파일 실패" >&2
  exit 1
fi
