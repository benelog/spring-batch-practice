# 업스트림 제보: 레퍼런스 Tracing 절의 핸들러 예시 오류

2026-08-04에 **이슈 [#5475](https://github.com/spring-projects/spring-batch/issues/5475)로 등록하고
[PR #5476](https://github.com/spring-projects/spring-batch/pull/5476)까지 올렸다.**
PR은 두 핸들러를 함께 등록하는 안으로 냈고, 이슈에 PR 링크를 댓글로 남겼다.
아래 본문이 실제로 등록한 이슈 내용이다.

등록 절차와 규칙은 [spring-contribution.md](spring-contribution.md)를 따른다.

## 초안 작성 전 확인한 것

- **중복 이슈 없음.** `TracingAwareMeterObservationHandler`로 검색한 이슈·PR은 0건,
  "tracing observation handler" 검색 결과도 없다.
- **이 예시는 6.0에서 새로 들어왔다.** 커밋 `ef1de538a`("Remove usage of Micrometer's global static
  meter registry", 2025-09-18, Resolves #4968)에서 추가됐다. 5.x 문서에는
  "tracing is enabled by default when using `@EnableBatchProcessing`"만 있고 핸들러 등록 예시가 없었다.
  현재 `main`(6.1.0-SNAPSHOT)에도 같은 코드가 그대로 있다.
- **부트 자동 구성 경로에서는 이 결함이 드러나지 않는다.** 부트 4.1.0의
  `TracingAndMeterObservationHandlerGroup.registerMembers()`는 `TracingObservationHandler` 계열을 먼저
  등록하고, `MeterObservationHandler`만 `TracingAwareMeterObservationHandler`로 감싼다.
  즉 래퍼가 요구하는 `TracingContext`를 넣어 주는 핸들러가 항상 함께 등록된다.
  레퍼런스가 보여 주는 수동 구성 경로에서만 실패한다.
- **잡이 FAILED로 끝나는 것이 아니라 `JobLauncher.run()`이 예외를 던진다.** 재현 프로젝트로 다시 확인했다.
  조사 기록의 "FAILED로 끝난다"는 서술보다 정확한 표현이라 이슈 본문에는 이쪽으로 적었다.

## 제목

```text
Reference documentation's tracing example registers a handler that creates no spans and fails the job
```

## 본문

````markdown
**Bug description**

The [Tracing section](https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html#tracing) of the reference documentation says to register an `ObservationHandler` "that supports tracing, such as `TracingAwareMeterObservationHandler`", and shows that handler as the only example.

That handler does not create spans. Its javadoc describes it as "a handler that can wrap another one and makes the tracing data available for it (e.g.: exemplars)". Registering it alone, exactly as documented, creates no spans and throws when the job observation is stopped.

**Environment**

Spring Batch 6.0.4 (the same text is in `main`), Micrometer Tracing 1.6.6 with the OpenTelemetry bridge, OpenTelemetry SDK 1.54.1, Java 21. No Spring Boot, since the documented configuration is a manual one, and no database: the job runs on a `ResourcelessJobRepository`.

**Steps to reproduce**

Configure the registry as documented and run any job:

```java
ObservationRegistry observationRegistry = ObservationRegistry.create();
observationRegistry.observationConfig()
        .observationHandler(new TracingAwareMeterObservationHandler<>(
                new DefaultMeterObservationHandler(meterRegistry), tracer));
```

The job and its steps are built with `.observationRegistry(observationRegistry)`, and spans are collected with the OpenTelemetry SDK `InMemorySpanExporter`. No span is created, and the exception thrown when the job observation is stopped propagates out of `JobLauncher.run()`:

```
java.lang.IllegalArgumentException: Context does not have an entry for key
  [class io.micrometer.tracing.handler.TracingObservationHandler$TracingContext]
	at io.micrometer.tracing.handler.TracingAwareMeterObservationHandler.onStop(TracingAwareMeterObservationHandler.java:78)
	at org.springframework.batch.core.job.AbstractJob.stopObservation(AbstractJob.java:370)
```

**Expected behavior**

What the same section promises: "a trace for each job execution and a span for each step execution".

**Minimal Complete Reproducible example**

https://github.com/benelog/spring-batch-practice/tree/main/issue-tracing-handler

Two tasklet steps, no Spring Boot. `./gradlew run` runs the same job with the documented handler, with a tracing handler and with both. `./gradlew test` asserts all three: it fails only on the documented one.

**Cause**

`TracingAwareMeterObservationHandler.onStop()` requires a `TracingContext`, which only a `TracingObservationHandler` implementation puts into the observation context. It is a decorator, usable only alongside a span-producing handler. Boot gets this right: `TracingAndMeterObservationHandlerGroup.registerMembers()` registers the `TracingObservationHandler` beans and only wraps `MeterObservationHandler` instances. So this affects the manual configuration shown in the documentation.

**Suggested fix**

Either register a span-producing handler:

```java
observationRegistry.observationConfig()
        .observationHandler(new DefaultTracingObservationHandler(tracer));
```

or, if the example was meant to cover metrics and tracing together (the metrics section right above registers a `DefaultMeterObservationHandler` on a bean of the same name), register both:

```java
observationRegistry.observationConfig()
        .observationHandler(new DefaultTracingObservationHandler(tracer))
        .observationHandler(new TracingAwareMeterObservationHandler<>(
                new DefaultMeterObservationHandler(meterRegistry), tracer));
```

The reproducer covers both: 3 spans with the first, 3 spans and 6 `spring.batch.*` meters with the second. The sentence above the snippet needs to change as well. I am happy to submit a PR for whichever the team prefers.

The example was introduced in 6.0 (commit ef1de538, "Remove usage of Micrometer's global static meter registry", #4968); the 5.x documentation had no handler example.
````

## 재현 프로젝트

`~/source/benelog/spring-batch-practice/issue-tracing-handler`에 만들었다.
부트를 쓰지 않는 그레이들 프로젝트다(레퍼런스가 보여 주는 수동 구성이 요점이라서).

```bash
cd ~/source/benelog/spring-batch-practice/issue-tracing-handler
./gradlew run     # 세 구성의 결과를 나란히 출력
./gradlew test    # 대조군 2건 통과, 문서대로 구성한 케이스 실패
```

`./gradlew test` 결과는 다음과 같다.

```text
TracingHandlerTests > a TracingObservationHandler creates one job span and one span per step (passes) PASSED
TracingHandlerTests > registering both handlers yields spans and metrics (passes) PASSED
TracingHandlerTests > the configuration shown in the reference documentation should create spans (fails) FAILED
3 tests completed, 1 failed
```

'README.md'는 위 이슈 본문과 같은 내용을 담고 있다.
커밋 `a6f217d`로 푸시했고, 본문에 넣은 링크가 열리는 것을 확인했다.

## PR에 어느 안을 냈나

**두 핸들러를 함께 등록하는 안**으로 냈다(PR #5476). 근거는 셋이다.

- 지금 예시가 `DefaultMeterObservationHandler`를 감싸고 있는 것 자체가 "메트릭 예시에 추적을 얹는다"는
  의도로 보인다. 바로 앞 Built-in metrics 절이 같은 이름(`observationRegistry`)의 빈에
  `DefaultMeterObservationHandler`를 등록하는 예시를 싣고 있어서, 추적 절 예시가 메트릭 핸들러를
  빼 버리면 두 절의 코드가 서로를 덮어쓰는 관계가 된다. 스프링 부트도 결국 두 핸들러를 함께 등록한다.
- 원래 의도를 살리면서 빠진 것(스팬을 만드는 핸들러)만 더하는 최소 수정이다.
- 실측으로 확인했다. 재현 프로젝트의 세 번째 실행이 이 구성이고 스팬 3건과 `spring.batch.*` 메트릭 6건이 모두 나온다.

단독 등록 안은 추적만 다루는 절이라는 점에서 더 단순하지만, 앞 절과 이어 읽을 때 메트릭이 사라지는 것을
독자가 알아채기 어렵다. 리뷰어가 단순한 쪽을 원하면 그때 줄이면 된다.

등록 순서는 요구 조건이 아니다. 두 핸들러를 반대 순서로 등록해도 이 시나리오에서는 스팬 3건과 메트릭 6건이
똑같이 나왔다. 그래서 이슈 본문에 "추적 핸들러를 먼저"라는 단정은 넣지 않았다.
다만 예시 코드는 부트와 같은 순서로 적어 두었다.

예시 코드 위의 "an `ObservationHandler` that supports tracing, such as
`TracingAwareMeterObservationHandler`" 문장이 잘못된 안내의 출발점이라 함께 고쳤다.

## PR에서 실제로 고친 것

브랜치 `GH-5475`, 커밋 `13732da5e`, 파일은
'spring-batch-docs/modules/ROOT/pages/spring-batch-observability/micrometer.adoc' 하나다(+8 -2).

- 안내 문장을 "an `ObservationHandler` that supports tracing, such as
  `TracingAwareMeterObservationHandler`"에서 "a `TracingObservationHandler`, such as
  `DefaultTracingObservationHandler`"로 바꿨다.
- 예시에 `DefaultTracingObservationHandler` 등록 한 줄을 더했다. 기존 래퍼 등록 줄은 남겼다.
- 래퍼가 스팬을 만들지 않는다는 NOTE를 예시 아래에 넣었다.

PR 본문에는 이슈에 없는 것만 적었다. 왜 두 핸들러 안을 골랐는지, 리뷰어가 단독 등록을 원하면
줄이겠다는 것, 등록 순서는 요구 조건이 아니지만 부트와 같은 순서로 적었다는 것이다. 전문은 다음과 같다.

````markdown
Resolves #5475

The example now registers a `DefaultTracingObservationHandler`, and the sentence above it no longer presents `TracingAwareMeterObservationHandler` as a handler that supports tracing on its own.

I kept the metrics handler in the example rather than replacing it, because both sections define a bean named `observationRegistry`: a tracing-only snippet here would quietly drop the metrics from the section above. Happy to reduce it to the tracing handler alone if you prefer the shorter example.

Handler order is not a requirement, the reversed order produced the same spans and meters, but I registered the tracing handler first to match `TracingAndMeterObservationHandlerGroup` in Spring Boot.
````

## 이 건에서 나온 일반 규칙

처음 올린 이슈·PR 본문이 문단마다 가로폭에 맞춰 줄을 접은 형태여서 2026-08-04에 둘 다 고쳐 올렸고,
재현 프로젝트의 'README.md'도 함께 고쳤다. 이때 정리한 규칙(가로폭 줄바꿈 금지, 본문 구성,
도입 시점 확인, 부트 없는 재현 프로젝트)은 [spring-contribution.md](spring-contribution.md)로 옮겼다.

**이 건에서 놓친 것**: 스프링 배치에 '.github/ISSUE_TEMPLATE/bug_report.md'가 있는데 확인하지 않고
본문 구조를 직접 잡았다. 같은 날 템플릿 항목(**Bug description** → **Environment** →
**Steps to reproduce** → **Expected behavior** → **Minimal Complete Reproducible example**)에 맞춰
이슈 본문을 다시 올렸다. 위에 옮겨 둔 것이 그 최종 본문이다.
원인과 수정안은 템플릿에 없는 항목이라 뒤에 이어 붙였고, 실제 동작은 재현 절차의 결과로 넣었다.
템플릿을 먼저 확인하라는 규칙도 같은 날 규칙 문서에 넣었다.

## 남은 것

- 리뷰 대응. 리뷰어가 단독 등록을 원하면 예시에서 래퍼 줄과 NOTE를 빼고 커밋을 스쿼시해 다시 올린다.
- 원고 15장은 `DefaultTracingObservationHandler` 단독 등록으로 이미 정정했고 그대로 둔다.
  책 예제는 추적만 보여 주므로 이슈의 수정안 선택과 무관하다.
