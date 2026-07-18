# Reproducer: `spring.batch.job.launch.count` is never recorded

Spring Boot 4.1.0 / Spring Batch 6.0.4, Java 17+.

The [reference documentation](https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html)
lists `spring.batch.job.launch.count` among the provided metrics, but the `JobOperator`
never receives an `ObservationRegistry` in common configurations, so this metric is
silently missing while all the other `spring.batch.*` metrics are recorded.

## Run

```bash
./gradlew bootRun
```

A single tasklet job runs and the app prints diagnostics on shutdown.

### Actual output

```text
[DIAG] spring.batch meters recorded = [spring.batch.job, spring.batch.job.active, spring.batch.step, spring.batch.step.active]
[DIAG] spring.batch.job.launch.count present = false
[DIAG] jobOperator bean class = jdk.proxy2.$Proxy...
[DIAG] jobOperator target class = org.springframework.batch.core.launch.support.TaskExecutorJobOperator, observationRegistry = ObservationRegistry.NOOP
```

Declaring an `ObservationRegistry` bean with the exact snippet from the reference
documentation does not change the outcome, and it introduces a second problem:

```bash
./gradlew bootRun --args='--spring.profiles.active=custom-registry'
```

```text
[DIAG] spring.batch.job.launch.count present = false
[DIAG] spring.batch.job timer count = 2 (the job ran once; a count of 2 means duplicated handlers)
```

The manually registered `DefaultMeterObservationHandler` and the handler that Spring
Boot's `ObservationRegistryPostProcessor` attaches to every `ObservationRegistry` bean
both record meters, so every `spring.batch.*` metric is counted twice in a Boot
application. A bare `ObservationRegistry.create()` bean avoids the duplication in Boot
(handlers are attached by Boot), but neither variant reaches the `JobOperator`.

## Analysis

Two mechanisms both fail to wire the `ObservationRegistry` into the `JobOperator`:

1. `DefaultBatchConfiguration#getObservationRegistry()` hardcodes `ObservationRegistry.NOOP`
   and passes it to `JobOperatorFactoryBean`. Spring Boot's `SpringBootBatchDefaultConfiguration`
   does not override it, so the Boot auto-configuration path always ends up with NOOP.
2. The fallback `BatchObservabilityBeanPostProcessor` checks `bean instanceof TaskExecutorJobOperator`,
   but the `jobOperator` bean is a JDK dynamic proxy created by `JobOperatorFactoryBean`
   (transaction advice + `JobOperator` interface), so the check is always false and the
   post processor silently skips it.

With `@EnableBatchProcessing` the result is the same for a different reason:
`BatchRegistrar` only adds the `observationRegistry` property reference when a bean
definition with that name already exists at registrar processing time, which is not the
case for Boot's auto-configured `ObservationRegistry` (deferred import). Declaring the
`ObservationRegistry` bean in the same configuration class that carries
`@EnableBatchProcessing` works around it on that path only.

Related history: spring-batch#4222 (observability bean post processor),
spring-batch#4226 (job launch counter).
