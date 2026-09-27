# 업스트림 제보: DataClassRowMapper가 행마다 findColumn 예외를 만듦

`DataClassRowMapper`는 스프링 배치가 아니라 스프링 프레임워크(spring-jdbc)의 클래스라 프레임워크 저장소에 올렸다.
2026-09-24에 이슈 없이 **[PR #37329](https://github.com/spring-projects/spring-framework/pull/37329)를 바로 올렸다.**
프레임워크의 CONTRIBUTING.md는 이슈를 먼저 만들 필요 없이 PR 본문에 맥락을 적으라고 한다.

- 수정 PR: https://github.com/spring-projects/spring-framework/pull/37329
  (브랜치 `data-class-row-mapper-column-lookup`, 커밋 `f3f92a00b6`, 로컬 클론 `~/source/benelog/spring-framework`)
- 성능 비교 댓글: https://github.com/spring-projects/spring-framework/pull/37329#issuecomment-5860566164
- 벤치마크: [perf-data-class-row-mapper](../perf-data-class-row-mapper)

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

## 보강: 채울 setter가 없으면 `mapRow()`의 칼럼 루프를 건너뛰기

2026-09-28에 spring-jdbc 7.0.9 소스를 읽으며 검토했고, 같은 날 PR #37329에 합쳐 올렸다.

### 문제

`BeanPropertyRowMapper.mapRow()`는 `constructMappedInstance()`로 객체를 만든 뒤 행마다 칼럼 레이블을 돌며 setter로 채울 속성을 찾는다.
record는 write method가 없어서 `initialize()`를 마친 뒤 `mappedProperties`와 `mappedPropertyNames`가 모두 비어 있다.
그래도 `mapRow()`는 행마다 `rs.getMetaData()`를 부르고, 칼럼마다 `lookupColumnName` → `StringUtils.delete` → `lowerCaseName` → `HashMap` 조회를 거친다. 조회 결과는 항상 `null`이다.
`bw.setBeanInstance(mappedObject)`의 introspection 캐시 조회도 쓸모가 없다.
PR #37329를 적용하면 `findColumnIndexes()`가 같은 칼럼 레이블 루프를 이미 한 번 돈다. 따라서 record 한 행에 칼럼 레이블 루프가 두 번 돌고, 그중 `mapRow()`의 것은 헛일이다.

### 생성 직후 무조건 반환하면 안 되는 이유

`DataClassRowMapper`는 record 전용이 아니다.
Javadoc이 "data class constructor … or classic bean property setter methods … (or even a combination of both)"를 지원한다고 적고 있다.
`initialize()`는 생성자 파라미터 이름을 `suppressProperty()`로 `mappedProperties`에서 빼고, `mapRow()`의 루프는 생성자가 받지 않은 setter 속성만 채운다.
`constructMappedInstance()`의 결과를 곧바로 반환하면 생성자와 setter를 섞은 클래스의 setter 쪽 필드가 비어 버린다.

### 수정안

조건은 `isRecord()`가 아니라 "setter로 채울 속성이 없음"으로 건다.
모든 속성을 생성자로 받는 일반 클래스나 Kotlin data class도 record와 같은 경우이기 때문이다.
`mappedProperties`는 `private`이므로 검사는 `DataClassRowMapper`가 아니라 `BeanPropertyRowMapper.mapRow()`에 들어간다.

```java
T mappedObject = constructMappedInstance(rs, bw);
if (CollectionUtils.isEmpty(this.mappedProperties) &&
        (!isCheckFullyPopulated() || CollectionUtils.isEmpty(this.mappedPropertyNames))) {
    // Nothing left to populate via setters (e.g. a record mapped through its constructor)
    return mappedObject;
}
bw.setBeanInstance(mappedObject);
```

- `mappedProperties`가 비어 있으면 루프 안의 `pd`가 항상 `null`이다. 이때 루프는 값도 채우지 않고 debug 로그도 남기지 않는다.
- 루프를 건너뛰어 사라지는 부수효과는 마지막의 `checkFullyPopulated` 검사 하나뿐이다. 이 검사는 빈 `populatedProperties`를 `mappedPropertyNames`와 비교한다.
- `suppressProperty()`는 `mappedProperties`에서만 이름을 지우고 `mappedPropertyNames`에서는 지우지 않는다. 그래서 생성자 인자이면서 setter도 있는 클래스에 `checkFullyPopulated=true`를 주면 기존 코드는 예외를 던진다. 두 번째 조건이 이 동작을 그대로 보존한다.
- record는 `mappedPropertyNames`도 비어 있으므로 `checkFullyPopulated`와 상관없이 조기 반환한다.
- 두 필드는 `@Nullable`이라 NullAway를 통과하도록 `CollectionUtils.isEmpty()`로 null과 빈 경우를 함께 처리한다. 이 시점에는 `constructMappedInstance()`의 `Assert`를 이미 통과했으므로 사실상 초기화되어 있다.
- `BeanWrapperImpl` 생성과 `initBeanWrapper()`는 없앨 수 없다. `constructMappedInstance()`가 이것을 `TypeConverter`로 써서 `ConversionService`를 적용하기 때문이다.

테스트는 `DataClassRowMapperTests`에 `staticQueryWithDataRecordSkipsSetterPopulation` 하나를 더했다.
`checkFullyPopulated=true`로 record를 매핑하고, `rs.getMetaData()`가 한 번만 불리는지(`findColumnIndexes()`에서만) 검증한다. 조기 반환을 빼면 두 번 불려 실패한다.
생성자와 setter를 섞은 클래스의 setter 쪽이 채워지는지는 기존 `staticQueryWithDataClassAndSetters`가 이미 확인하므로 따로 넣지 않았다.
생성자+setter 혼합 클래스와 `checkFullyPopulated`의 조합이 예외를 던지는 기존 동작은 이 수정에서 바꾸지 않고, 필요하면 별도 이슈로 다룬다.

### 프레임워크를 고치지 않고 우회하는 방법

서브클래스에서는 `mappedProperties`가 보이지 않으므로 타입을 record로 제한한다.
`initBeanWrapper()`와 `constructMappedInstance()`는 `protected`라서 재료는 다 있다.

```java
final class RecordRowMapper<T extends Record> extends DataClassRowMapper<T> {

    RecordRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        BeanWrapperImpl bw = new BeanWrapperImpl();
        initBeanWrapper(bw); // applies the ConversionService used as the constructor's TypeConverter
        return constructMappedInstance(rs, bw);
    }
}
```

### 판단

- 절감되는 것은 행마다 칼럼 수만큼의 문자열 처리와 맵 조회다. 남는 작업(칼럼 값 읽기, 타입 변환, 리플렉션 생성자 호출)이 더 무겁다.
- `DataClassRowMapper`의 Javadoc도 "designed to provide convenience rather than high performance"라고 적고 있어 우선순위는 낮게 받을 가능성이 크다.
- 처음에는 `BeanPropertyRowMapper`를 건드리는 다른 주제라서 별도 PR로 올리려 했다.
  그러나 측정해 보니 #37329의 칼럼 조회 변경만으로는 camelCase 별칭 쿼리의 이득이 작았고(H2 −10%, MySQL −16%), 조기 반환이 더해져야 개선이 뚜렷했다. 그래서 #37329에 합쳤다.
  2026-09-28 기준 #37329는 `status: waiting-for-triage` 상태이고 리뷰가 없어 합치는 부담이 적었다.

### 올린 방법

- 기존 커밋 `a6e92c94a0`에 amend해 커밋 하나(`f3f92a00b6`)로 합치고 `--force-with-lease`로 포크 브랜치에 푸시했다. 커밋 메시지에는 조기 반환을 설명하는 문단을 더했다.
- JDK 25로 `./gradlew :spring-jdbc:check`를 돌려 통과했다.
- PR 본문은 고치지 않고, 변경 내용과 동기(record 같은 불변 객체를 반환하는 것이 좋은 관행이니 그 선택이 싸야 한다), 측정 결과를 댓글로 달았다.

### 성능 측정

[perf-data-class-row-mapper](../perf-data-class-row-mapper)에 JMH 벤치마크를 만들었다.
10개 컴포넌트 record로 1,000행을 `JdbcTemplate.query`로 읽는다. 비교 대상 세 커밋에서 `./gradlew :spring-jdbc:jar`로 만든 jar를 `-PspringJdbcJar`로 바꿔 끼우고, 나머지 스프링 모듈은 `7.1.0-SNAPSHOT` 저장소에서 받는다.
MySQL은 같은 머신의 Docker(8.4)로 띄웠다.

쿼리 1회 평균 시간(µs)이다.

| db | 칼럼 레이블 | main | PR 첫 버전 | 현재 PR | 직접 쓴 매퍼(참고) |
|---|---|---:|---:|---:|---:|
| H2 | `first_name` | 10,982 | 1,207 | 800 | 54 |
| H2 | `first_name as firstName` | 1,185 | 1,070 | 805 | 56 |
| MySQL | `first_name` | 8,072 | 1,955 | 1,775 | 1,251 |
| MySQL | `first_name as firstName` | 2,296 | 1,925 | 1,794 | 1,246 |

- 첫 측정(fork 2개)은 MySQL 수정 전 값의 한 fork가 amend·푸시 작업과 겹쳐 오차가 ±45%까지 벌어졌다. 다른 작업 없이 fork 3개로 다시 쟀다.
- H2 in-memory에서는 직접 쓴 매퍼가 54µs라 `DataClassRowMapper`의 매핑 비용이 거의 전부다. 반면 MySQL은 쿼리 자체가 1.25ms라 개선 비율이 H2보다 작게 보인다.
