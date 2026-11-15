# 업스트림 제보: 레퍼런스 'Testing Individual Steps' 절의 메서드 이름 오류

원고 저장소의 블로그 2편(`editorial/drafts/blog/02_배치테스트기법.adoc`)을 쓰면서 레퍼런스 'Unit Testing' 장을 다시 읽다가 발견했다.
2026-11-15에 **이슈 없이 [PR #5667](https://github.com/spring-projects/spring-batch/pull/5667)로 올렸다.**
CONTRIBUTING.md는 오타 수정처럼 사소한 변경이면 이슈를 먼저 만들지 않아도 된다고 한다("unless the change is truly trivial, e.g. typo fixes").
같은 파일에서 `startJob()` 메서드 이름 오타를 고친 PR #5148(2025-12-10 병합)도 이슈 없이 들어갔다.

- PR: https://github.com/spring-projects/spring-batch/pull/5667 (브랜치 `fix-testing-docs-start-step`, 커밋 `45aca50e4`, 로컬 클론 `~/source/benelog/spring-batch`)
- 올린 직후 DCO 검사는 통과했다.

등록 절차와 규칙은 [spring-contribution.md](spring-contribution.md)를 따른다.

## 초안 작성 전 확인한 것

- **본문이 메서드 이름을 틀리게 적는다.** 둘째 문장은 `JobOperatorTestUtils`에 `launchStep` 메서드가 있다고 하지만 이 클래스의 메서드는 `startStep`(오버로드 4개)이다.
  바로 아래 예제도 `jobOperatorTestUtils.startStep("loadFileStep")`을 호출한다.
  `launchStep`은 6.0에서 지원 중단된 `JobLauncherTestUtils`와 `StepRunner`의 메서드다.
- **6.0에서 들어온 문제다.** 커밋 `9855dbd4d`("Update documentation", 2025-07-23)가 이 절을 `JobLauncherTestUtils`에서 `JobOperatorTestUtils`로 바꾸면서 클래스 이름, 예제 코드, 셋째 문장의 메서드 이름은 고쳤지만 둘째 문장의 `launchStep`은 그대로 남겼다.
  v6.0.0-M1부터 들어 있고 v6.0.6과 `main`(커밋 `899ccc33e`)의 문장이 같다.
- **같은 문단 첫 문장의 'It these cases'는 6.0 이전부터 있던 오타다.** 같은 문단이라 함께 고쳤다.
- **중복 PR·이슈 없음.** `launchStep`과 "Testing Individual Steps"로 검색한 PR은 0건이다.
  "testing.adoc" 검색 결과 6건(#5310, #5313, #5148, #4835, #4806, #523)은 모두 닫혔거나 병합됐고 이 문장을 다루지 않는다.

## PR에서 고친 것

'spring-batch-docs/modules/ROOT/pages/testing.adoc' 하나다(+2 -2).

- ``contains a method called `launchStep` `` → ``contains a method called `startStep` ``
- `It these cases` → `In these cases`

문서 파일의 기존 줄 접기는 그대로 두었다.

## 커밋 메시지

```text
Fix the method name in the "Testing Individual Steps" section

The section said that JobOperatorTestUtils contains a method called
launchStep, while the method is startStep, as the example right below it
shows. launchStep belongs to the deprecated JobLauncherTestUtils: the prose
kept the old name when the section moved to JobOperatorTestUtils in 6.0.

This commit also fixes the "It these cases" typo in the same paragraph.

Signed-off-by: Sanghyuk Jung <sanghyuk.jung@navercorp.com>
```

## PR 제목과 본문

```text
Fix the method name in the "Testing Individual Steps" section
```

````markdown
The [Testing Individual Steps](https://docs.spring.io/spring-batch/reference/testing.html#testingIndividualSteps) section says that `JobOperatorTestUtils` contains a method called `launchStep`, but the method is `startStep`, which is what the example right below it calls. `launchStep` is the method of the deprecated `JobLauncherTestUtils`: the name was left over when the section was updated for `JobOperatorTestUtils` in 9855dbd4 ("Update documentation"), so the 6.0.x documentation has the same text.

This also fixes the "It these cases" typo in the same paragraph.
````

연결할 이슈가 없어서 `Resolves` 줄은 넣지 않았다.
6.0.x 문서도 같다는 것만 본문에 적었고, 백포트 여부는 메인테이너가 정하게 두었다.

## 남은 것

- 리뷰 대응. 병합되면 이 문서에 병합 커밋과 백포트 여부를 적는다.
- 블로그 2편의 기법 9는 이미 `startStep()`으로 적혀 있어서 원고 쪽은 고칠 것이 없다.
