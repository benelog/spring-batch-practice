# Reproducer: the tracing example in the reference documentation creates no spans and fails the job

Spring Batch 6.0.4 / Micrometer Tracing 1.6.6 / OpenTelemetry SDK 1.54.1, Java 21.
No Spring Boot: the point of the reproducer is the manual configuration shown in the
documentation.

## Bug description

The [Tracing section](https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html#tracing)
of the reference documentation tells the reader to define an `ObservationRegistry` with an
`ObservationHandler` "that supports tracing, such as `TracingAwareMeterObservationHandler`",
and shows that handler as the only example.

`TracingAwareMeterObservationHandler` does not create spans. Its javadoc describes it as "a
handler that can wrap another one and makes the tracing data available for it (e.g.:
exemplars)", so it is a decorator around a `MeterObservationHandler`. Registering it alone,
exactly as documented, produces no spans at all and makes the job fail.

## Steps to reproduce

```bash
./gradlew run
```

The same two-step job runs twice. The first run registers the handler from the documentation,
the second registers a `DefaultTracingObservationHandler`. Spans are collected with the
OpenTelemetry SDK `InMemorySpanExporter`.

```java
// as documented
observationRegistry.observationConfig()
		.observationHandler(new TracingAwareMeterObservationHandler<>(
				new DefaultMeterObservationHandler(meterRegistry), tracer));
```

### Actual output

```text
java.lang.IllegalArgumentException: Context does not have an entry for key
  [class io.micrometer.tracing.handler.TracingObservationHandler$TracingContext]
	at io.micrometer.tracing.handler.TracingAwareMeterObservationHandler.onStop(TracingAwareMeterObservationHandler.java:78)
	...
	at org.springframework.batch.core.job.AbstractJob.stopObservation(AbstractJob.java:370)
	at org.springframework.batch.core.job.AbstractJob.execute(AbstractJob.java:338)

=== TracingAwareMeterObservationHandler (reference documentation) ===
job exit status         = LAUNCH_THREW
job failure             = java.lang.IllegalArgumentException: Context does not have an entry for key [...]
finished span count     = 0
spring.batch.* meters   = 2
```

### Expected output

What the same section promises, "a trace for each job execution and a span for each step
execution". This is what the second run produces:

```text
=== DefaultTracingObservationHandler ===
job exit status         = COMPLETED
finished span count     = 3
  span: name=spring.batch.step traceId=990f732c9f677f85edafbc00e68ff80a parentSpanId=c2890924400a0cb4
  span: name=spring.batch.step traceId=990f732c9f677f85edafbc00e68ff80a parentSpanId=c2890924400a0cb4
  span: name=spring.batch.job traceId=990f732c9f677f85edafbc00e68ff80a parentSpanId=0000000000000000
```

Also asserted as tests:

```bash
./gradlew test
```

`tracingObservationHandlerCreatesSpans` passes; `documentedExampleShouldCreateSpans` fails.

## Root cause

`TracingAwareMeterObservationHandler.onStop()` calls `context.getRequired(TracingContext.class)`,
and only a `TracingObservationHandler` implementation puts a `TracingContext` into the
observation context. The wrapper is therefore usable only in addition to a span-producing
handler; it cannot be the handler that "supports tracing" on its own.

Spring Boot's auto-configuration does this correctly:
`TracingAndMeterObservationHandlerGroup#registerMembers` registers the
`TracingObservationHandler` beans first and only wraps `MeterObservationHandler` instances with
`TracingAwareMeterObservationHandler`. So the problem shows up only on the manual configuration
path that the reference documentation describes.

## Suggested fix

Use a span-producing handler in the example:

```java
@Bean
public ObservationRegistry observationRegistry(Tracer tracer) {
	ObservationRegistry observationRegistry = ObservationRegistry.create();
	observationRegistry.observationConfig()
			.observationHandler(new DefaultTracingObservationHandler(tracer));
	return observationRegistry;
}
```

If the intent was to have both metrics and tracing, the example needs both handlers, with the
tracing handler registered first, mirroring what Boot does:

```java
observationRegistry.observationConfig()
		.observationHandler(new DefaultTracingObservationHandler(tracer))
		.observationHandler(new TracingAwareMeterObservationHandler<>(
				new DefaultMeterObservationHandler(meterRegistry), tracer));
```

## Impact

Readers who do not use Spring Boot's auto-configuration and follow the documentation literally
get no tracing data and a job that fails. The example was introduced in 6.0 (commit ef1de538,
"Remove usage of Micrometer's global static meter registry", #4968); the 5.x documentation had
no handler example, so this affects 6.0.x and `main`.

## Workaround

Register a `TracingObservationHandler` implementation such as `DefaultTracingObservationHandler`.
