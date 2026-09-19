# 업스트림 제보: 레퍼런스 스키마 부록의 인덱스 권고 표가 실제 쿼리와 다름

원고 6장 6.1.2절의 인덱스 안내를 쓰면서(원고 저장소 커밋 `c706323`) 발견했다.
2026-09-19에 **이슈 [#5549](https://github.com/spring-projects/spring-batch/issues/5549)로 등록하고
[PR #5550](https://github.com/spring-projects/spring-batch/pull/5550)까지 올렸다.** 이슈에 PR 링크를 댓글로 남겼다.
아래 본문이 실제로 등록한 이슈·PR 내용이다.

- 이슈: https://github.com/spring-projects/spring-batch/issues/5549
- 수정 PR: https://github.com/spring-projects/spring-batch/pull/5550 (브랜치 `GH-5549`, 커밋 `094ee41d5`, 로컬 클론 `~/source/benelog/spring-batch`)

등록 절차와 규칙은 [spring-contribution.md](spring-contribution.md)를 따른다.

## 초안 작성 전 확인한 것

- **표는 `main`과 `6.0.x`가 같다.** 2009-02-20 커밋 `96eafb5e4`(BATCH-1043)에 들어온 뒤 내용이 바뀐 적이 없다.
- **`STEP_NAME = ? and JOB_EXECUTION_ID = ?` 행은 4.2.0부터 코드와 어긋났다.** 커밋 `62a8f441a`(2019-09-02, BATCH-2716,
  4.2.0.RC1)에서 `JdbcStepExecutionDao.GET_LAST_STEP_EXECUTION`이 `BATCH_JOB_EXECUTION`과 조인해
  `JE.JOB_INSTANCE_ID = ? AND SE.STEP_NAME = ?`로 조회하게 됐다. `COUNT_STEP_EXECUTIONS`(커밋 `b50c665f0`, 4.3.0)도 같은 조인이다.
  6.0.5와 `main`의 세 JDBC DAO(`JdbcJobInstanceDao`, `JdbcJobExecutionDao`, `JdbcStepExecutionDao`)는 WHERE 절이 같다.
  호출처는 `SimpleStepHandler`의 `getLastStepExecution`과 `countStepExecutions`로, 스텝 실행 전마다 부른다.
- **`VERSION = ?` 행은 실제로는 `STEP_EXECUTION_ID = ? AND VERSION = ?`다.** 기본 키가 한 행으로 좁히므로 `VERSION` 인덱스는 소용없다.
- **`BATCH_JOB_EXECUTION_PARAMS`의 `JOB_EXECUTION_ID = ?`(`FIND_PARAMS_FROM_ID`)가 표에 없다.**
  `JdbcJobExecutionDao.getJobExecution(long)`이 잡 실행을 읽을 때마다 부른다. `TaskExecutorJobLauncher`는 기존 잡 인스턴스로
  실행할 때 `getJobExecutions(jobInstance)`로 인스턴스의 모든 실행을 읽으므로 재시작 때 실행 수만큼 나간다.
  6.0.0~6.0.3에서는 `SimpleJobRepository.update(StepExecution)`이 청크마다 이 조회를 했고, 그것이 #5360이다.
  6.0.4의 커밋 `692acba43`이 청크 경로를 고쳤고, 커밋 `65e85d09f`(2026-05-26)가 오라클 DDL에만 `BATCH_JOB_EXEC_PARAMS_IDX`를 넣었다.
- **`BATCH_STEP_EXECUTION`을 `JOB_EXECUTION_ID = ?`로 읽는 `GET_STEP_EXECUTIONS`는 처음에 표에 넣지 않았다가 두 번째 커밋으로 보탰다.**
  처음에는 호출처가 `SimpleJobExplorer`뿐이라고 봤는데, `SimpleJobRepository`가 `SimpleJobExplorer`를 상속하므로
  `getLastJobExecution`·`getJobExecutions`·`getJobExecution`이 모두 `fillJobExecutionDependencies`를 거쳐 이 조회를 한다.
  잡 실행을 읽을 때마다 나가는 조회라 `BATCH_JOB_EXECUTION_PARAMS` 행과 같은 빈도로 적었다(커밋 `c2ea6f0a1`, 2026-09-19).
- **중복 이슈 없음.** "Recommendations for Indexing" 검색 결과는 #4551(2024-02-15, open, 마일스톤 없음)과 그 PR #5419, #5425뿐이다.
  #4551은 외래 키 칼럼을 추가로 안내하자는 내용이고 표의 부정확성은 다루지 않는다. 두 PR(2026-06-09, 06-10, 같은 작성자)은
  외래 키 소절을 덧붙이는 내용이며 리뷰나 메인테이너 반응이 없다. 본문 끝에 'Generated with Claude Code'가 붙어 있다.
- **이슈 템플릿.** 버그도 기능 제안도 아니라 '.github/ISSUE_TEMPLATE/miscellaneous.md'에 해당한다. 항목이 없는 템플릿이라 절 제목(H2)으로 구조를 잡았다.
  라벨은 `status: waiting-for-triage`가 자동으로 붙는다.

## 제목

```text
Indexing recommendations table in the schema appendix does not match the queries issued since 4.2
```

## 본문

````markdown
The [Recommendations for Indexing Metadata Tables](https://docs.spring.io/spring-batch/reference/schema-appendix.html#recommendationsForIndexingMetaDataTables) section lists the `WHERE` clauses that the DAO implementations use, so that users can decide which columns to index. The table has not changed since it was written and no longer matches the queries in `JdbcStepExecutionDao` and `JdbcJobExecutionDao`. I checked against 6.0.5 and `main`, where the queries below are identical.

## Rows that do not match the code

### `BATCH_STEP_EXECUTION`, `STEP_NAME = ? and JOB_EXECUTION_ID = ?`, "Before each step execution"

No query in `JdbcStepExecutionDao` uses this clause. Since 4.2.0 (commit 62a8f441, BATCH-2716), `SimpleStepHandler` finds the last step execution through `JobRepository#getLastStepExecution(JobInstance, String)`, and the DAO runs a join. `countStepExecutions` (4.3.0, commit b50c665f) uses the same join:

```sql
SELECT ...
FROM BATCH_JOB_EXECUTION JE
    JOIN BATCH_STEP_EXECUTION SE ON SE.JOB_EXECUTION_ID = JE.JOB_EXECUTION_ID
WHERE JE.JOB_INSTANCE_ID = ? AND SE.STEP_NAME = ?
```

So the columns hit before each step execution are `BATCH_JOB_EXECUTION.JOB_INSTANCE_ID`, `BATCH_STEP_EXECUTION.JOB_EXECUTION_ID` (the join column) and `BATCH_STEP_EXECUTION.STEP_NAME`.

### `BATCH_STEP_EXECUTION`, `VERSION = ?`, "On commit interval"

The actual clause is `STEP_EXECUTION_ID = ? AND VERSION = ?` (`UPDATE_STEP_EXECUTION` and `DELETE_STEP_EXECUTION`). The primary key already narrows it to one row, so an index on `VERSION` does not help. The table's title says primary keys are excluded, and this row should be excluded for the same reason.

### `BATCH_JOB_EXECUTION_PARAMS`, `JOB_EXECUTION_ID = ?` is missing

`JdbcJobExecutionDao#getJobExecution(long)` loads the parameters with `FIND_PARAMS_FROM_ID` every time a job execution is loaded. On a launch for an existing job instance, `TaskExecutorJobLauncher` loads every execution of the instance, so the query runs once per execution. The table has no primary key, and this is the column #5360 was about: since 6.0.4 the Oracle script creates `BATCH_JOB_EXEC_PARAMS_IDX` for it, the other scripts do not, and the appendix does not mention it.

### `BATCH_JOB_EXECUTION`, `JOB_INSTANCE_ID = ?`, "Every time a job is restarted"

Still accurate for `GET_LAST_JOB_EXECUTION_ID` and `GET_JOB_EXECUTION_IDS_BY_INSTANCE_ID`, but `TaskExecutorJobLauncher` runs both on every launch for an existing job instance, not only on a restart. Minor compared with the rows above.

## Related

#4551 asks to add foreign key columns to this section (PRs #5419 and #5425). This issue is about the existing rows not matching the code, so I kept it separate. I will submit a PR that updates the table.
````

## PR 본문

````markdown
Resolves #5549

The table now lists the clauses that `JdbcJobInstanceDao`, `JdbcJobExecutionDao` and `JdbcStepExecutionDao` run in 6.x. The `BATCH_STEP_EXECUTION` row with `STEP_NAME = ? and JOB_EXECUTION_ID = ?` is replaced by the join on `BATCH_JOB_EXECUTION` that `getLastStepExecution` and `countStepExecutions` use, the `VERSION = ?` row is removed because that clause always comes with the primary key, and a row for `BATCH_JOB_EXECUTION_PARAMS.JOB_EXECUTION_ID` is added.

I also added a note that the unique constraint on `JOB_NAME` and `JOB_KEY` is usually backed by an index and that the Oracle script already creates one for `BATCH_JOB_EXECUTION_PARAMS`, so that readers do not create duplicate indexes. Happy to drop the note if you prefer the table alone.

The foreign key columns discussed in #4551 are left to that issue.
````

## PR에서 고친 것

파일은 'spring-batch-docs/modules/ROOT/pages/schema-appendix.adoc' 하나다(+8 -4).

- `BATCH_JOB_EXECUTION` 행의 빈도를 "Every time a job is launched for an existing job instance (for example, a restart)"로 바꿨다.
- `BATCH_JOB_EXECUTION_PARAMS`의 `JOB_EXECUTION_ID = ?` 행을 더했다.
- `BATCH_STEP_EXECUTION`의 `VERSION = ?` 행을 지웠다.
- `BATCH_STEP_EXECUTION`의 `STEP_NAME = ? and JOB_EXECUTION_ID = ?` 행을 `BATCH_JOB_EXECUTION`과의 조인 행으로 바꿨다.
- (두 번째 커밋) `BATCH_STEP_EXECUTION`의 `JOB_EXECUTION_ID = ?` 행을 더했다. PR 댓글로 이유를 남겼다.
- 표 아래에 유일성 제약과 오라클 인덱스를 알리는 NOTE를 넣었다.

## 남은 것

- 리뷰 대응. NOTE 단락을 빼 달라고 하면 표만 남기고 다시 올린다.
- 리뷰가 끝나면 커밋 두 개를 하나로 스쿼시해 강제 푸시한다. 규칙은 커밋 하나인데, 두 번째 커밋을 올릴 때 자동 모드에서 강제 푸시가 막혀 미뤘다.
- 원고 6장에 넣었던 인덱스 안내(원고 저장소 커밋 `c706323`)는 2026-09-19에 되돌렸다(커밋 `41d8688`).
  PR 결과가 나온 뒤 다시 넣을지를 포함한 후속 할 일은 https://github.com/benelog/personal-task/issues/393 에서 관리한다.
