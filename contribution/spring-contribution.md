# 스프링 프로젝트에 이슈·PR을 올릴 때 지키는 규칙

스프링 배치를 검증하다가 찾은 버그를 <https://github.com/spring-projects/spring-batch>에 보고하고
고칠 때 따르는 절차다. 근거는 스프링 배치 저장소의
[CONTRIBUTING.md](https://github.com/spring-projects/spring-batch/blob/main/CONTRIBUTING.md)와
[ISSUE_REPORTING.md](https://github.com/spring-projects/spring-batch/blob/main/ISSUE_REPORTING.md)다.

## 커밋 작성자와 본문 형식

- 커밋하는 사람(author)과 sign-off 하는 사람은 실명으로 쓴다. CONTRIBUTING.md가 닉네임이 아닌 실명을 요구한다.
  이름과 이메일은 로컬 저장소에만 `git config`로 설정하고 전역 설정은 건드리지 않는다.
- **가로폭에 맞춰 줄을 접지 않는다.** 이슈 본문, PR 본문, 재현 프로젝트의 'README.md'는 한 문단을
  한 줄로 쓴다. 코드 블록과 로그 인용은 원문 그대로 둔다. 원고 리포의 문서는 반대로 문장 단위 줄바꿈이
  원칙이니 섞이지 않게 주의한다.

## 클론과 리모트

포크는 `git@github.com:benelog/spring-batch.git`이고 로컬 클론은 `~/source/benelog/spring-batch`다.

- `origin`: 포크
- `upstream`: `https://github.com/spring-projects/spring-batch.git`

작업 전에 `git fetch upstream --tags`로 동기화한다.
특정 릴리스의 소스를 볼 때는 `git show v6.0.4:<경로>`를 쓴다.

## 빌드 (JDK 25로 한다)

그레이들이 아니라 메이븐 프로젝트다. 래퍼는 `./mvnw`다.
`pom.xml`의 `java.version`은 17이지만 **빌드는 JDK 25로 해야 한다.**
CI(`.github/workflows/continuous-integration.yml`)도 25를 쓴다.
JDK 21이나 17로 돌리면 NullAway·Error Prone 플러그인이 컴파일 중에 죽는다
(`An unhandled exception was thrown by the Error Prone static analysis plugin`).
`.mvn/jvm.config`의 `--add-exports` 설정이 25 기준으로 맞춰져 있어서다.

```bash
export JAVA_HOME=/home/benelog/.sdkman/candidates/java/25-tem
./mvnw -pl spring-batch-core -am -DskipTests install
./mvnw -pl spring-batch-core -o test -Dtest='<테스트 클래스 패턴>' -DfailIfNoSpecifiedTests=false
```

수정을 실제 애플리케이션에서 확인하려면 위 `install`로 로컬 저장소에 스냅숏을 넣고,
재현 프로젝트의 `build.gradle`에 `mavenLocal()`과 버전 오버라이드를 임시로 더한다.

```gradle
repositories { mavenLocal(); mavenCentral() }
ext['spring-batch.version'] = '6.0.5-SNAPSHOT'
```

확인이 끝나면 이 두 줄은 되돌린다. 재현 프로젝트는 릴리스 버전으로 돌아가야 이슈 본문과 맞다.

## 이슈 등록

1. **먼저 중복을 확인한다.** GitHub 검색이 코드 식별자를 잘 못 찾으므로 여러 방법을 겹쳐 쓴다.

    ```bash
    gh api -X GET search/issues --raw-field q='repo:spring-projects/spring-batch <키워드> in:body,title' \
      --jq '.total_count, (.items[]|"\(.number) [\(.state)] \(.title)")'
    gh search issues --repo spring-projects/spring-batch "<문장>" --include-prs --limit 15 \
      --json number,title,state --template '{{range .}}{{.number}} [{{.state}}] {{.title}}{{"\n"}}{{end}}'
    ```

    닫힌 이슈도 함께 본다. 비슷해 보이는 이슈가 있으면 무엇이 다른지 본문에 적는다.

2. **언제 들어온 문제인지 확인한다.** 도입 커밋을 찾고 어느 릴리스부터 영향을 받는지 본다.
   본문의 영향 범위를 사실로 적을 수 있고, 리뷰어가 회귀인지 처음부터 그랬는지 바로 안다.

    ```bash
    git log --oneline -S '<식별자나 문구>' --all -- <경로>
    git tag --contains <커밋>
    git show <태그>:<경로>        # 그 릴리스 시점의 내용
    ```

3. **최소 재현 예제를 만든다.** ISSUE_REPORTING.md는 zip 첨부를 요구하지만, 이 저장소
   (`~/source/benelog/spring-batch-practice`)에 `issue-<주제>` 폴더로 만들고 그 링크를 본문에 넣는
   방식으로 대신해 왔다. 재현 폴더에는 다음을 갖춘다.

    - 문제를 그대로 보여주는 진단 출력. 부트 프로젝트면 `./gradlew bootRun`, 부트 없이 재현하면
      `./gradlew run`이다. 문제가 수동 설정에서만 나타난다면 부트를 빼고 만든다.
      부트 자동 구성이 문제를 가려 버리기 때문이다.
    - `./gradlew test`로 대조군은 통과하고 문제 케이스는 실패하는 테스트
    - 이슈 본문 그대로 쓸 수 있는 영어 'README.md'. 이슈를 올린 뒤에는 이슈·PR 번호를 여기에 적는다.

4. **본문 구성은 그 프로젝트의 템플릿을 먼저 따른다.** 저장소의 '.github/ISSUE_TEMPLATE'을 열어
   해당하는 템플릿의 항목 이름과 순서를 그대로 쓴다. 템플릿이 없을 때만 직접 구조를 잡고,
   그때는 굵은 머리말이 아니라 절 제목(H2, H3)을 쓴다.

    스프링 배치는 '.github/ISSUE_TEMPLATE/bug_report.md'가 있고, 항목이 굵은 머리말로 정해져 있다.
    **Bug description** → **Environment** → **Steps to reproduce** → **Expected behavior** →
    **Minimal Complete Reproducible example** 순이다. Environment에는 버전(스프링 부트, 스프링 배치,
    자바, 쓰는 DB)을 적는다. 템플릿에 없는 내용(원인 분석, 수정안)은 뒤에 이어 붙인다.

    항목을 채울 때 지키는 것은 다음과 같다.

    - **문제를 일으키는 코드 조각은 본문에 직접 싣는다.** 재현 프로젝트 링크만 걸면 무엇이 문제인지
      바로 보이지 않는다. 전체 재현은 링크로 넘긴다.
    - 같은 내용을 두 항목에 겹쳐 적지 않는다. 회피 방법이 수정안과 같으면 따로 적지 않는다.
    - 수정안이 둘 이상이면 하나로 몰지 말고 나란히 제시하고 선택은 메인테이너에게 맡긴다.
      제시하는 수정안은 재현 프로젝트에서 실제로 돌려보고 결과를 함께 적는다.

## PR 올리기

1. 이슈 번호로 토픽 브랜치를 만든다: `git checkout -b GH-1234 upstream/main`
2. 코드 스타일은 스프링 프레임워크 코드 스타일을 따른다. 들여쓰기는 탭이다.
   `./mvnw -pl <모듈> spring-javaformat:validate`로 확인하고, 어긋나면 `spring-javaformat:apply`로 맞춘다.
3. 새 파일에는 아파치 라이선스 헤더를 넣고, 고친 파일은 헤더의 연도 범위를 갱신한다
   (예: `Copyright 2002-2023` → `Copyright 2002-2026`).
4. 새로 추가하는 공개 API에는 `@since` 태그를 단다.
5. 동작이 바뀌면 JUnit 테스트를 함께 낸다. 기존 테스트 클래스를 먼저 찾아 거기에 더한다.
6. 커밋은 하나로 스쿼시한다. 히스토리는 선형으로 유지하고, PR 전에 `upstream/main`에 리베이스한다.
7. 관련 테스트를 돌려서 통과시킨 뒤 올린다.
8. 포크에 푸시하고 PR을 연다: `git push origin GH-1234`

### 커밋 메시지 틀

```text
<무엇을 고쳤는지 한 줄. 명령형, 마침표 없음>

<문제를 설명하는 문단. 무엇이 잘못 동작했는지 사실만 적는다.>

<수정 방식을 설명하는 문단.>

Resolves #1234

Signed-off-by: <실명> <이메일>
```

`Signed-off-by` 트레일러는 DCO 요구사항이라 빠뜨리면 CI에서 막힌다. `git commit -s`로 자동으로 붙일 수 있다.

### PR 본문

- **PR 템플릿이 있으면 그 형식을 먼저 따른다.** 저장소의 '.github/PULL_REQUEST_TEMPLATE.md'를 확인한다.
  스프링 배치의 것은 항목을 요구하지 않고 리베이스·스쿼시·테스트·sign-off를 확인하라는 안내문이라,
  본문 형식은 아래대로 정하면 된다. 항목을 요구하는 템플릿이면 그 항목을 그대로 쓴다.
- 형식을 직접 잡을 때 절이 필요할 만큼 길면 절 제목(H2, H3)을 쓴다. 굵은 머리말로 대신하지 않는다.
  세 문단 정도로 끝나면 절 없이 쓴다.
- **이슈에 이미 적은 내용을 되풀이하지 않는다.** 증상, 재현 절차, 영향은 이슈에 있다.
- PR 본문에는 이 PR에서만 볼 수 있는 것을 적는다: 무엇을 어떻게 고쳤는지, 왜 그 방식인지,
  검토자가 봐야 할 판단(대안, 호환성, 부작용), 추가한 테스트.
- 첫 줄에 `Resolves #1234`나 `Closes #1234`로 이슈를 연결한다.
- PR을 올린 뒤 해당 이슈에 링크를 댓글로 남긴다(CONTRIBUTING.md 요구사항).

## 사례

- [listener-duplicate-callback.md](listener-duplicate-callback.md) — 이슈 #5466, PR #5467
- [chunk-error-listener-annotation.md](chunk-error-listener-annotation.md) — 이슈 #5468, PR #5469
- [tracing-handler-issue.md](tracing-handler-issue.md) — 이슈 #5475, PR #5476 (레퍼런스 문서 수정)
- [resources-item-reader-builder.md](resources-item-reader-builder.md) — 이슈 #5487, PR #5488 (기능 제안, ResourcesItemReader 빌더)
- [schema-appendix-index-table.md](schema-appendix-index-table.md) — 이슈 #5549, PR #5550 (레퍼런스 스키마 부록의 인덱스 권고 표 수정)
- [data-class-row-mapper-column-lookup.md](data-class-row-mapper-column-lookup.md) — 스프링 프레임워크 PR #37329 (DataClassRowMapper의 칼럼 조회 예외 제거)
