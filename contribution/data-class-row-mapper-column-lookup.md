# 업스트림 제보: DataClassRowMapper가 행마다 findColumn 예외를 만듦

`DataClassRowMapper`는 스프링 배치가 아니라 스프링 프레임워크(spring-jdbc)의 클래스라 프레임워크 저장소에 올렸다.
2026-09-24에 이슈 없이 **[PR #37329](https://github.com/spring-projects/spring-framework/pull/37329)를 바로 올렸다.**
프레임워크의 CONTRIBUTING.md는 이슈를 먼저 만들 필요 없이 PR 본문에 맥락을 적으라고 한다.

- 수정 PR: https://github.com/spring-projects/spring-framework/pull/37329
  (브랜치 `data-class-row-mapper-column-lookup`, 커밋 `a6e92c94a0`, 로컬 클론 `~/source/benelog/spring-framework`)

## 문제

생성자 파라미터마다 `rs.findColumn(lowerCaseName(name))`을 먼저 부르고, `SQLException`이 나면 `rs.findColumn(underscoreName(name))`으로 다시 찾는다.
파라미터가 camelCase이고 칼럼이 snake_case이면 행마다, 그런 파라미터마다 예외가 하나씩 생긴다.
2022년 커밋 `90103b0ae9`(gh-28243, 직접 이름 매칭 추가)부터 이 순서다.

## 초안 작성 전 확인한 것

- **gh-37297(2026-09-18, 7.0.10)이 같은 문제를 `SimplePropertyRowMapper`에서만 고쳤다.**
  위르겐 횔러가 커밋 `2a15cd498a`로 조회 순서를 뒤집어 snake_case를 먼저 찾게 했다. `DataClassRowMapper`는 그대로 남았다.
  순서만 뒤집으면 `select birth_date as birthDate`처럼 camelCase 별칭을 쓰는 쿼리는 여전히 행마다 예외가 난다.
- **`BeanPropertyRowMapper`는 `findColumn`을 쓰지 않는다.** `initialize()`에서 속성을 소문자·snake_case 두 키로 맵에 넣고,
  `mapRow()`에서 행마다 `ResultSetMetaData`의 칼럼 레이블을 돌며 맵을 조회한다. `DataClassRowMapper.mapRow()`도 결국 이 루프를 돈다.

## 수정 방식

- `initialize()`에서 snake_case 이름 → 파라미터 인덱스, 소문자 이름 → 파라미터 인덱스 맵 두 개를 멤버로 만든다. `BeanPropertyRowMapper`의 `mappedProperties`와 같은 방향(이름 → 대상)이다.
  행마다 칼럼 레이블을 돌며 두 맵을 조회하고, 파라미터별 매칭 결과는 `int[]` 두 개에 담는다. 중복 레이블은 `findColumn`처럼 첫 번째 칼럼을 쓴다.
- 조회 순서는 gh-37297 이후의 `SimplePropertyRowMapper`에 맞춰 snake_case 먼저, 소문자 이름 다음이다.
- 맵에서 못 찾은 파라미터만 try/catch `findColumn`(순서는 같게)으로 넘긴다. 칼럼이 없을 때의 예외와 드라이버 고유 매칭을 보존하려는 것이다.
  이름이 겹치는 파라미터(예: `fooBar`와 `foo_bar`)는 `putIfAbsent`에서 밀린 쪽이 이 경로로 가므로 결과는 전과 같다.
- 테스트 두 개를 `DataClassRowMapperTests`에 더했다. `findColumn`을 부르지 않는지 검증하는 테스트는 수정 전 `main`에서 `NeverWantedButInvoked`로 실패한다.
  `AbstractRowMapperTests.Mock`에 `getResultSet()` 접근자를 더했다.

### 첫 구현에서 바꾼 것

처음 올린 커밋(`4b5b6364ae`)은 행마다 칼럼 레이블 → 인덱스 `HashMap`을 새로 만들었고, 순서는 기존대로 소문자 이름이 먼저였다.
칼럼 구성은 `ResultSet`마다 달라서 그 방향의 맵은 멤버로 둘 수 없다. 매퍼 하나를 여러 쿼리·스레드가 공유하기 때문이다.
반면 이름 → 파라미터 맵은 매핑 대상 클래스에만 의존하므로 초기화 때 한 번 만들 수 있다. 그래서 맵 방향을 뒤집어 행마다 생기는 맵 생성을 없앴다.
`initialize()`는 상위 클래스 생성자에서 불리므로 새 필드에 초기화 식을 붙이면 채운 값이 덮어써진다. 기존 필드처럼 초기화 식 없이 `@Nullable`로 두었다.
PR 본문도 테스트 설명을 빼고 두 문단으로 줄였다.

## 빌드

JDK 25로 `./gradlew :spring-jdbc:check`를 돌려 통과했다. 파라미터 이름 배열이 `@Nullable String[]`이라 보조 메서드의 파라미터도 `@Nullable`로 두지 않으면 NullAway가 컴파일을 막는다.
커밋 메시지는 프레임워크 규칙(제목 55자, 본문 72자 줄바꿈)을 따랐고 `Signed-off-by`를 붙였다. 파일 헤더가 `2002-present`라 연도 갱신은 필요 없다.
