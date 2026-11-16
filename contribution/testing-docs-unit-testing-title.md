# 업스트림 제보: 레퍼런스 'Unit Testing' 장의 제목이 내용과 다름

원고 저장소의 블로그 2편(`editorial/drafts/blog/02_배치테스트기법.adoc`) 도입부에 스프링 배치와 스프링 프레임워크 레퍼런스의 단위 테스트 범위를 비교하는 목록을 넣으면서 정리했다.
2026-11-17에 **이슈 [#5668](https://github.com/spring-projects/spring-batch/issues/5668)로 등록하고 [PR #5669](https://github.com/spring-projects/spring-batch/pull/5669)까지 올렸다.**
이슈에는 PR 링크와 함께 메인테이너를 멘션하는 댓글을 남겼다. 아래 본문이 실제로 등록한 이슈·PR·댓글 내용이다.

- 이슈: https://github.com/spring-projects/spring-batch/issues/5668
- 수정 PR: https://github.com/spring-projects/spring-batch/pull/5669 (브랜치 `GH-5668`, 커밋 `494762556`, 로컬 클론 `~/source/benelog/spring-batch`)
- 올린 직후 DCO 검사는 통과했다.

등록 절차와 규칙은 [spring-contribution.md](spring-contribution.md)를 따른다.

## 초안 작성 전 확인한 것

- **페이지 파일과 앵커는 이미 'testing'이다.** 소스는 `spring-batch-docs/modules/ROOT/pages/testing.adoc`이고 페이지 앵커는 `[[testing]]`, URL은 `testing.html`이다.
  파일 이름을 바꿀 필요가 없으므로 이 변경으로 페이지 URL은 바뀌지 않는다. 바뀌는 것은 제목뿐이다.
  `nav.adoc`은 `* xref:testing.adoc[]`로 링크 텍스트를 비워 두어서 사이드바에는 페이지 제목 'Unit Testing'이 그대로 나온다.
- **문서 첫 페이지는 이미 다른 이름으로 부른다.** `index.adoc` 26행이 `xref:testing.adoc[Unit and Integration Testing] :: Job and Step testing facilities and APIs.`다. v6.0.6과 `main`이 같다.
- **장 서문이 스스로 종단간 테스트를 다룬다고 밝힌다.** "The Spring core documentation covers how to unit and integration test with Spring in great detail, so it will not be repeated here. It is important, however, to think about how to “end to end” test a batch job, which is what this chapter covers."
  다섯 절 가운데 스프링 없이 `new`로 객체를 만드는 예제는 'Mocking Domain Objects' 하나뿐이다.
- **'unit test'가 나오는 곳과 판단.** 행 번호는 `main`(커밋 `899ccc33e`) 기준이다.

    | 행 | 원문 | 판단 |
    | --- | --- | --- |
    | 3 | `= Unit Testing` | 바꾼다 |
    | 5~6 | "it is extremely important to unit test any code ...", "how to unit and integration test with Spring" | 일반적인 뜻이라 둔다 |
    | 12~13 | `[[creatingUnitTestClass]]` `== Creating a Unit Test Class` | 제목만 바꾸고 앵커는 둔다 |
    | 15 | "For the unit test to run a batch job, the framework must load the job's `ApplicationContext`." | 바꾼다 |
    | 280 | "writing unit and integration tests for Spring Batch components" | 둘 다 가리키므로 둔다 |
    | 300~301, 324~326 | 'Mocking Domain Objects'의 "unit test" 네 곳 | 실제로 `new`로 만드는 단위 테스트라 둔다 |

- **제목은 1.x부터 그대로다.** 1.1.1.RELEASE의 DocBook `docs/src/site/docbook/reference/testing.xml`부터 `<title>Unit Testing</title>`이고, 'Creating a Unit Test Class' 절은 2.0.0.RC1에 들어왔고(그때는 앵커 없음), 앵커 `creatingUnitTestClass`는 3.0.0.M1에는 있다.
  4.3.9(`spring-batch-docs/asciidoc/testing.adoc`), 5.0.0(`spring-batch-docs/src/main/asciidoc/testing.adoc`)을 거쳐 커밋 `67853f22d`("Documentation Migration")로 안토라로 옮겨졌다.
  v6.0.6과 `main`의 앞 16행은 같다.
- **JUnit으로 작성하는 테스트를 모두 unit test로 부른 것으로 보인다.** 1.1.1의 'End To End Testing Batch Jobs' 절은 `fixedLengthImportJob`을 처음부터 끝까지 실행하고 DB를 확인하는 코드를 "The following unit test code assures it processes correctly"라고 소개한다.
  지금도 'Creating a Unit Test Class' 절이 만드는 예제 클래스 이름은 `SkipSampleFunctionalTests`다. 2.0.0.RC1~3.0의 같은 절은 `@RunWith(SpringJUnit4ClassRunner.class)`를 "Indicates that the class should use Spring's JUnit facilities"로 설명한다.
  문서가 근거를 밝힌 것은 아니므로 이슈 본문에도 추정(my guess)으로 적었다.
- **스프링 프레임워크 7.0.10의 구분.** 'Unit Testing' 절(https://docs.spring.io/spring-framework/reference/testing/unit.html)은 "The POJOs that make up your application should be testable in JUnit or TestNG tests, with objects instantiated by using the `new` operator, without Spring or any other container."라고 쓴다.
  애플리케이션 컨텍스트를 올리는 테스트는 'Integration Testing' 절(https://docs.spring.io/spring-framework/reference/testing/integration.html)이 다룬다: "The Spring TestContext Framework provides consistent loading of Spring `ApplicationContext` instances and `WebApplicationContext` instances as well as caching of those contexts."
- **마일스톤.** `main`은 `6.1.0-SNAPSHOT`이고 6.1.0은 2026-11-19가 기한이다(RC1은 2026-10-22에 나왔다). 7.0.0 마일스톤도 열려 있다(Derby 지원 제거 #5270, 스프링 리트라이 의존 제거 #4868 등 5건).
- **중복 이슈 없음.** "Unit Testing" 검색 결과 6건(#5442, #4684, #3699, #2422, #498, #1737)은 장 제목을 다루지 않는다. #498은 2014년의 오타 수정이다.
  "Creating a Unit Test Class" 검색 결과는 0건이다.
- **이슈 템플릿.** 버그도 기능 제안도 아니라 '.github/ISSUE_TEMPLATE/miscellaneous.md'에 해당한다. 항목이 없는 템플릿이라 절 제목(H2)으로 구조를 잡았다.
- **열려 있는 PR #5667과 겹치지 않는다.** 같은 파일이지만 #5667은 'Testing Individual Steps' 절(161행 부근)만 고친다.

## 제목

```text
"Unit Testing" chapter title does not match its content
```

## 본문

````markdown
The [Unit Testing](https://docs.spring.io/spring-batch/reference/testing.html) chapter is titled "Unit Testing", but most of it is about tests that load the job's `ApplicationContext` and run a job or a step. Its introduction says so itself: "The Spring core documentation covers how to unit and integration test with Spring in great detail, so it will not be repeated here. It is important, however, to think about how to “end to end” test a batch job, which is what this chapter covers." Of the five sections, only "Mocking Domain Objects" shows a test that creates its objects with `new` and does not use Spring.

The Spring Framework reference draws the line differently. Its [Unit Testing](https://docs.spring.io/spring-framework/reference/testing/unit.html) section describes tests "with objects instantiated by using the `new` operator, without Spring or any other container", and tests that load an `ApplicationContext` through the Spring TestContext Framework are covered in [Integration Testing](https://docs.spring.io/spring-framework/reference/testing/integration.html). Spring Batch users read both references, so the same term covering different scopes is confusing. For example, a reader who follows "Creating a Unit Test Class" ends up with a test that loads the whole job configuration, which the Spring Framework reference would call an integration test.

The Spring Batch documentation is not consistent about this either: the page file is `testing.adoc` with the anchor `testing`, and the [index page](https://docs.spring.io/spring-batch/reference/index.html) links to it as "Unit and Integration Testing", while the page title and the navigation say "Unit Testing".

My guess is that the title comes from calling every test written with JUnit a unit test. The chapter has been titled "Unit Testing" since the 1.x reference, where the End To End Testing section introduced a test that runs `fixedLengthImportJob` and checks the database as "the following unit test code". Today, the class that "Creating a Unit Test Class" sets up is still named `SkipSampleFunctionalTests`.

## Proposed changes

In `spring-batch-docs/modules/ROOT/pages/testing.adoc`:

- Chapter title `= Unit Testing`: either `= Testing`, which matches the file name and the anchor, or `= Unit and Integration Testing`, which matches the index page. Either works for me.
- Section title `== Creating a Unit Test Class` → `== Creating a Test Class`, keeping the `creatingUnitTestClass` anchor so that existing links keep working.
- "For the unit test to run a batch job, the framework must load the job's `ApplicationContext`." → "For a test to run a batch job, the framework must load the job's `ApplicationContext`."

The other occurrences of "unit test" can stay: the introduction uses the term in its general sense, and "Mocking Domain Objects" shows tests that are unit tests in the Spring Framework sense.

The text above is the same in 6.0.6 and `main`. I will open a PR with these changes.
````

'Proposed changes' 아래 마지막 문장은 PR을 바로 이어서 올렸으므로 초안의 "If this sounds good, I can open a PR."을 "I will open a PR with these changes."로 바꿔 등록했다.

## PR에서 고친 것

'spring-batch-docs/modules/ROOT/pages/testing.adoc' 하나다(+4 -4). 문서 파일의 기존 줄 접기는 그대로 두었다.

- 3행 `= Unit Testing` → `= Testing`. 이슈에서 나란히 제시한 두 안 가운데 파일 이름·앵커와 맞는 쪽을 골랐고, 다른 안으로 바꾸기 쉽다고 PR 본문과 댓글에 적었다.
- 13행 `== Creating a Unit Test Class` → `== Creating a Test Class`. 앵커 `creatingUnitTestClass`는 그대로 두었다.
- 15행 "For the unit test to run a batch job," → "For a test to run a batch job,"
- 301행 "the following unit test for the listener's in the preceding example" → "the following unit test for the listener in the preceding example". 이슈에는 없는 오타지만 같은 파일이라 함께 고쳤다(#5667과 같은 방식).

`nav.adoc`은 링크 텍스트를 비워 두어서 고칠 필요가 없고, `index.adoc`의 "Unit and Integration Testing"은 장 내용을 설명하는 문구로도 맞아서 그대로 두었다.
다른 페이지에서 'Unit Testing'이나 'Unit Test Class'를 가리키는 곳은 없다.

## 커밋 메시지

```text
Rename the "Unit Testing" chapter to "Testing"

Most of the chapter describes tests that load the job's
ApplicationContext and run a job or a step, which the Spring Framework
reference calls integration tests. The page file and its anchor are
already named "testing", and the index page links to the chapter as
"Unit and Integration Testing".

This commit changes the chapter title to "Testing", renames the
"Creating a Unit Test Class" section to "Creating a Test Class" while
keeping its anchor, and updates the first sentence of that section. It
also fixes the "listener's in" typo in the "Mocking Domain Objects"
section.

Resolves #5668

Signed-off-by: Sanghyuk Jung <sanghyuk.jung@navercorp.com>
```

## PR 제목과 본문

```text
Rename the "Unit Testing" chapter to "Testing"
```

````markdown
Resolves #5668

This uses `= Testing` for the chapter title, the first of the two options in the issue, because it matches the file name and the `testing` anchor. Switching to `= Unit and Integration Testing` is a one-line change if you prefer it.

The `creatingUnitTestClass` anchor is kept, so links to the renamed "Creating a Test Class" section keep working. The other occurrences of "unit test" are unchanged. This also fixes the "listener's in" typo in the "Mocking Domain Objects" section.
````

## 이슈에 단 댓글

메인테이너 Mahmoud Ben Hassine(`fmbenhassine`)을 멘션했다. 2025-06 이후 `upstream/main`의 `spring-batch-docs` 커밋 121건 가운데 88건이 그의 것이고, 앞선 제보(#5487·#5488)도 그가 병합했다.
CONTRIBUTING.md가 요구하는 PR 링크 댓글과 버전 제안 댓글을 하나로 합쳤다.
새 버전이 적기라는 근거는 페이지 URL이 아니라 사이드바에 보이는 장 제목과, 앵커까지 바꿀 경우의 절 URL 변경으로 잡았다. 페이지 파일은 이미 `testing.adoc`이라 URL이 바뀌지 않는다.

```markdown
@fmbenhassine I opened #5669 for this. Since it changes the chapter title shown in the navigation, I think Spring Batch 6.1 or 7.0 would be a good time for it rather than a 6.0.x patch release. The page URL stays the same because the file is already `testing.adoc`, and the PR keeps the `creatingUnitTestClass` anchor. If you prefer to also rename that anchor to match the new section title, that would change the section's URL, which is another reason to do it in a new version. The PR uses `= Testing` as the chapter title, and I can switch it to `= Unit and Integration Testing` if you prefer.
```

https://github.com/spring-projects/spring-batch/issues/5668#issuecomment-6928800307

## 남은 것

- 리뷰 대응. 메인테이너가 다른 제목이나 앵커 변경을 고르면 같은 브랜치에 고쳐서 하나로 스쿼시한다. 병합되면 이 문서에 병합 커밋과 대상 버전을 적는다.
- 6.1.0은 2026-11-19가 기한이라 이 PR은 6.1.x나 그다음 버전에 들어갈 가능성이 크다.
- 원고 쪽은 고칠 것이 없다. 블로그 2편의 비교 목록은 지금 문서 제목('Unit Testing')을 기준으로 적었으므로, 제목이 바뀌면 링크 텍스트만 다시 본다.
