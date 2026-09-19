# ResourcesItemReader 빌더 추가 제안 (기능 제안)

`MultiResourceItemReaderBuilder`에 패턴 지원을 제안한 이슈 #5056·PR #5071의 후속으로,
빌더가 아예 없는 ``ResourcesItemReader``에도 빌더를 만들면서 같은 패턴 지원을 넣자고 2026-08-09에 제안했다.
버그 보고가 아니라 기능 제안이라 재현 프로젝트는 만들지 않았다.

- 이슈: https://github.com/spring-projects/spring-batch/issues/5487
- PR: https://github.com/spring-projects/spring-batch/pull/5488 (브랜치 `GH-5487`, 로컬 클론 `~/source/benelog/spring-batch`)
- 선행 건: 이슈 #5056, PR #5071 (`MultiResourceItemReaderBuilder`에 같은 패턴 지원, 같은 날 함께 merge됨)

## 2026-09-16 후속: merge와 maintainer의 refine

두 건 모두 2026-09-16에 maintainer(fmbenhassine)가 merge했고, 마일스톤은 6.1.0-M2다.
PR은 그대로 merge한 뒤 별도 커밋으로 API 모양을 바꿨다.

| 건 | merge 커밋 | refine 커밋 |
| --- | --- | --- |
| #5056 / PR #5071 (`MultiResourceItemReaderBuilder`) | `0d96bd0` | `8ce6762` "Refine contribution #5071" |
| #5487 / PR #5488 (`ResourcesItemReaderBuilder`) | `1209216` | `930ef88` "Refine contribution #5488" |

maintainer가 바꾼 것과 그 이유(PR #5071 코멘트에 설명):

- `filesPattern(String)`을 없애고 `resources(String... locationPatterns)` 오버로드로 대체했다.
  빌더가 리소스 종류에 중립적이어야 한다는 이유다. 접두어를 `"file:"`로 고정하면
  `PathMatchingResourcePatternResolver`가 이미 지원하는 `classpath:`·`classpath*:`를 쓸 수 없다.
- 접두어를 붙이지 않고 호출자가 고르게 했다. 예: `.resources("file:/home/logs/*.csv")`.
- `filesPattern` 필드와 빌드 시점의 우선순위 판단을 없애고, 해석 결과를 기존 `resources` 필드에 바로 넣는다.
  같은 것을 설정하는 두 경로가 생기는 걸 피하기 위해서다.
- 레퍼런스 문서와 6.1 what's new에 두 빌더를 실었다. `ResourcesItemReader`는 이번에 처음 레퍼런스 문서에 들어갔다.

따라서 위 '요약'의 세 메서드 구성과 `"file:"` 접두어 설명은 PR을 낼 당시의 내용이고, 6.1.0-M2부터는
`resources(Resource...)`, `resources(String...)`, `name(String)`이다. 책에서 이 빌더를 쓰면 접두어를 포함한
`file:` 패턴으로 적어야 한다.

답글은 이슈가 아니라 PR #5071에 단다. maintainer의 근거 설명이 거기에 있고, PR #5488의 마감 코멘트도
#5071을 가리키므로 한 곳에 답하면 두 건을 갈음한다. 이슈는 마일스톤 추적용이라 건드리지 않는다.

## 요약

``ResourcesItemReader``는 `org.springframework.batch.infrastructure.item.file` 패키지에서 유일하게 빌더가 없는 리더다.
Javadoc은 패턴으로 설정하라고 권하지만 XML 설정의 `ResourceArrayPropertyEditor` 얘기라, 자바 설정에서는 패턴 해석을 사용자가 직접 해야 한다.

새 `ResourcesItemReaderBuilder`는 `resources(Resource...)`, `filesPattern(String)`, `name(String)` 세 메서드만 둔다.
리더에 `saveState`·`strict` 속성이 없고, `name`은 클래스명 기반 기본 키가 있어 필수로 강제하지 않았다.
`filesPattern()`은 PR #5071과 이름·의미를 맞췄다: 빌드 시점에 `PathMatchingResourcePatternResolver`가 `"file:"` 접두어를 붙여 해석하고, `IOException`은 `IllegalArgumentException`으로 바꾼다.

## 작업하며 확인한 것

- main 브랜치는 6.1.0-SNAPSHOT이라 `@since 6.1`을 달았다.
- 새 파일의 라이선스 헤더는 최근 관례대로 단일 연도(`Copyright 2026`)로 쓴다.
- 테스트 리소스 파일은 'resource1.txt'·'resource2.txt'로 지었다.
  PR #5071이 같은 디렉터리에 'test1.txt'·'test2.txt'를 추가하므로 두 PR이 모두 병합돼도 충돌하지 않게 하기 위해서다.
- 패턴 해석 결과의 순서는 파일 시스템에 달려 있어, 테스트는 순서 대신 파일명 집합으로 검증했다.
