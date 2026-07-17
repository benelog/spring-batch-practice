#!/bin/bash
# 에이전트가 파일을 수정할 때마다 실행되는 훅.
# 자바 파일이 수정됐으면 곧바로 컴파일해서 오류를 에이전트에게 돌려준다.

input=$(cat)

file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)
if [ -z "$file_path" ]; then
  # 도구에 따라 입력 JSON의 형식이 다르므로 .java 언급 여부로 보완한다.
  case "$input" in
    *'.java'*) file_path="unknown.java" ;;
  esac
fi

case "$file_path" in
  *.java) ;;
  *) exit 0 ;;
esac

cd "${CLAUDE_PROJECT_DIR:-$(dirname "$0")/..}" || exit 0

output=$(./gradlew compileJava compileTestJava 2>&1)
if [ $? -ne 0 ]; then
  echo "컴파일 실패: 아래 오류를 보고 코드를 고쳐라." >&2
  printf '%s\n' "$output" | tail -n 40 >&2
  exit 2
fi
exit 0
