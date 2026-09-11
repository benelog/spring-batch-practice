# 청크 에러 콜백을 애너테이션으로 등록할 수 없는 문제 (스프링 배치 6.0.4)

12장(`event-listener.adoc`)을 6.0.4로 검증하다가 찾았다.
16장 교열 반영(2026-07-29) 때 이 규칙을 맺음말 'AGENTS.md' 전문에서 뺄지 판단하면서 근거를 정리했다.

## 재현 프로젝트

`~/source/benelog/spring-batch-practice/issue-chunk-error-listener`

```bash
cd ~/source/benelog/spring-batch-practice/issue-chunk-error-listener
./gradlew bootRun   # 세 잡을 돌려 실제로 호출된 콜백 목록을 출력
./gradlew test      # 대조군 2건은 통과하고 문제 케이스 2건은 실패
```

이슈로 올릴 영어 본문은 그 프로젝트의 `README.md`에 있다.

## 요약

청크 기반 스텝(`StepBuilder.chunk(int)` → ``ChunkOrientedStepBuilder``)에서

- `ChunkListener` 인터페이스를 구현하면 `onChunkError(Exception, Chunk)`가 호출된다.
- `@OnChunkError`를 붙인 메서드는 **호출되지 않는다.**
- `@AfterChunkError`를 붙인 메서드도 **호출되지 않는다.**

같은 클래스에 붙인 `@BeforeChunk`, `@AfterChunk`는 정상 동작해서 빠뜨리기 쉽다.
에러도 경고 로그도 없다.

## 실측

```text
[DIAG] ChunkListener interface    = [interface:beforeChunk, interface:afterChunk, interface:beforeChunk, interface:onChunkError]
[DIAG] @OnChunkError              = [annotation:beforeChunk, annotation:afterChunk, annotation:beforeChunk]
[DIAG] @AfterChunkError (5.x)     = []
```

## 원인

두 가지가 겹쳤다.

**첫째, `StepListenerMetaData`에 `OnChunkError` 항목이 없다.**
`@OnChunkError`는 6.0에서 새로 생긴 애너테이션이고 자바독에도
`Expected signature: void onChunkError(Exception, Chunk)`라고 적혀 있는데, 애너테이션과
콜백 메서드의 대응 관계를 담은 열거형에는 항목이 추가되지 않았다(v6.0.4 기준).

```java
BEFORE_CHUNK("beforeChunk", "before-chunk-method", BeforeChunk.class, ChunkListener.class, Chunk.class),
AFTER_CHUNK("afterChunk", "after-chunk-method", AfterChunk.class, ChunkListener.class, Chunk.class),
AFTER_CHUNK_ERROR("afterChunkError", "after-chunk-error-method", AfterChunkError.class, ChunkListener.class,
        ChunkContext.class),
// OnChunkError 항목 없음
```

`ChunkOrientedStepBuilder#listener(Object)`는 `@OnChunkError`를 **찾기는 한다**.
찾아서 `StepListenerFactoryBean` 프록시까지 만든다. 그런데 그 프록시가 콜백을 연결할 때
쓰는 것이 위 열거형이라, `onChunkError`에는 아무것도 연결되지 않은 프록시가 등록된다.

```java
// ChunkOrientedStepBuilder#listener(Object), v6.0.4
listenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), OnChunkError.class));  // 찾는다
...
if (!listenerMethods.isEmpty()) {
    StepListenerFactoryBean factory = new StepListenerFactoryBean();
    factory.setDelegate(listener);
    this.stepListeners.add((StepListener) factory.getObject());  // 연결은 안 된다
}
```

**둘째, `AFTER_CHUNK_ERROR` 항목이 옛 메서드를 가리킨다.**
`@AfterChunkError`가 대응하는 메서드는 deprecated된 `afterChunkError(ChunkContext)`인데,
``ChunkOrientedStep``은 `onChunkError(Exception, Chunk)`만 호출한다.
`afterChunkError(ChunkContext)`를 호출하는 것은 ``TaskletStep``뿐이다.
게다가 `ChunkOrientedStepBuilder#listener(Object)`의 스캔 목록에 `AfterChunkError`가 없어서,
그 애너테이션만 가진 클래스는 등록조차 되지 않는다.

`@BeforeChunk`, `@AfterChunk`는 이슈 #4961(수정 PR #4969)에서 `Chunk` 시그니처로 옮겨졌다.
그때 `AFTER_CHUNK_ERROR`는 함께 옮기지 않았고, 새로 만든 `@OnChunkError`의 항목도 넣지 않았다.

## 보고와 수정

2026-07-29에 스프링 배치에 보고했다.

- 이슈: https://github.com/spring-projects/spring-batch/issues/5468
- 수정 PR: https://github.com/spring-projects/spring-batch/pull/5469 (브랜치 `GH-5468`, 로컬 클론 `~/source/benelog/spring-batch`)

수정은 `StepListenerMetaData`에 항목 하나를 더하는 것이다.
`ON_WRITE_ERROR`가 같은 `(Exception, Chunk)` 모양이라 그것을 따랐다.

```java
ON_CHUNK_ERROR("onChunkError", "on-chunk-error-method", OnChunkError.class, ChunkListener.class, Exception.class,
        Chunk.class),
```

패치를 적용한 6.0.5-SNAPSHOT으로 재현 프로젝트를 다시 돌려 확인했다.

```text
[DIAG] @OnChunkError = [annotation:beforeChunk, annotation:afterChunk, annotation:beforeChunk, annotation:onChunkError]
```

`@AfterChunkError`는 고치지 않았다. 6.2에서 없어질 메서드를 가리키는 애너테이션이라 살리는 방향이 맞지 않았다.
이슈 본문에는 남겨 뒀다.

### 등록 전에 확인한 기존 이슈

2026-07-29 기준으로 같은 내용의 이슈는 없었다. `OnChunkError`로 검색하면 0건이다.
가까운 것은 다음 셋인데 모두 다른 문제다.

- #4961 / #4969 (closed, 6.0.0-M3): `@BeforeChunk`·`@AfterChunk`를 자바독대로 `Chunk`를 받게 고쳤다.
  `AFTER_CHUNK_ERROR`는 그대로 뒀다.
- #5297 (closed): ``SimpleStepBuilder``에서 `@AfterChunkError`에 ``ChunkContext``를 쓰면
  `IllegalArgumentException`이 난다. 이쪽은 예외가 나고, 이 문제는 조용히 넘어간다.
- #5226 (closed): ``ItemReader``에 단 `ChunkListener` 콜백이 6.x에서 호출되지 않는다.

## 책에 반영한 것

- 12장 '스텝의 리스너' 절: 애너테이션으로 등록할 수 없다는 사실과 원인.
  처음에는 WARNING 박스였으나 2026-07-30에 본문으로 풀고, 이슈 #5468과 수정 PR #5469를 각주로 달았다.
- 부록 2(16장) '리스너 인터페이스 시그니처 변화' 절: 표의 `@AfterChunkError` 행이 `@OnChunkError`로의
  대체를 안내하되 6.0.4에서는 동작하지 않음을 표시. 상세 설명은 12장으로 몰고,
  표 아래 본문에서는 한 문장으로 12장 '스텝의 리스너' 절을 가리키기만 한다(2026-07-30).
- 12장 'AGENTS.md에 적어 두기' 절: 규칙에 `(스프링 배치 6.0.4 기준)`을 달고,
  버전이 올라가면 지워야 할 규칙임을 스니펫 아래에 설명했다(커밋 792dc85).
- 맺음말의 'AGENTS.md' 전문에서는 이 규칙을 뺐다. 프레임워크를 올리면 필요 없어질 수 있는
  규칙이라는 사유를 전문에 적었다. 예제 리포의 'AGENTS.md' 3개에서도 뺐다(커밋 c237275).

## 관련 문서

- [spring-contribution.md](spring-contribution.md) — 스프링 프로젝트에 이슈·PR을 올릴 때 지키는 규칙
- [listener-duplicate-callback.md](listener-duplicate-callback.md) — 같은 장에서 찾은 다른 리스너 버그(#5466)
