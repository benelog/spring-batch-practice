# 업스트림 제보: NamedParameterUtils가 '-'나 '/'로 끝나는 SQL에서 ArrayIndexOutOfBoundsException을 던짐

스프링 프레임워크 PR [#37088](https://github.com/spring-projects/spring-framework/pull/37088)(SQL 주석 안의 이름 있는 파라미터 옵션)과 같은 메서드를 다시 읽다가 찾았다.
2026-09-24에 이슈 없이 **[PR #37330](https://github.com/spring-projects/spring-framework/pull/37330)을 올렸다.**

- 수정 PR: https://github.com/spring-projects/spring-framework/pull/37330
  (브랜치 `named-parameter-trailing-comment-start`, 커밋 `8bf5b0e71d`, 로컬 클론 `~/source/benelog/spring-framework`)

## 문제

`skipCommentsAndQuotes()`는 `START_SKIP`의 첫 글자가 맞으면 `--`, `/*`의 두 번째 글자를 `statement[position + j]`로 비교하는데 길이를 확인하지 않는다.
SQL이 `-`나 `/` 한 글자로 끝나면(`select * from t where a = :a -`, `/`) 드라이버가 문법 오류를 알리기 전에 파서가 `ArrayIndexOutOfBoundsException`을 던진다.
2012년 모듈 이름 변경(`02a4473c62`) 이전부터 있던 코드다. 비슷한 이슈는 찾지 못했다.

## 수정 방식

- 비교하기 전에 `position + j >= statement.length`면 주석 시작이 아닌 것으로 본다. 한 줄 수정이다.
- `NamedParameterUtilsTests`에 파라미터화 테스트(4건)를 더했다. 수정 전에는 모두 AIOOBE로 실패한다.
- JDK 25로 `./gradlew :spring-jdbc:check`를 돌려 통과했다.
- #37088도 같은 메서드를 고친다. 바꾸는 줄이 달라 충돌은 크지 않을 것이다.
- PR 본문 초안에 있던 "#37088 작업 중에 찾았다"는 문장은 저자 검토에서 뺐다.
