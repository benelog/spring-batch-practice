# issue-batch-observability-plain

Plain Spring (non-Boot) companion to [issue-batch-observability](../issue-batch-observability):
shows that the `spring.batch.job.launch.count` wiring in Spring Batch 6.0.4 is sensitive to
configuration-class registration order even without Spring Boot.

`BatchRegistrar` (imported by `@EnableBatchProcessing`) adds the `observationRegistry`
property reference to the `jobOperator` bean definition only when a bean definition named
`observationRegistry` already exists while the registrar runs.

## Run

```bash
./gradlew run
```

Three contexts run the same tasklet job. The `ObservationRegistry` bean uses the exact snippet
from the [reference documentation](https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html)
(manual `DefaultMeterObservationHandler`, which is correct without Boot).

| Scenario | Registration order | `launch.count` |
| --- | --- | --- |
| A | `RegistryConfig`, then `@EnableBatchProcessing` class | recorded |
| B | `@EnableBatchProcessing` class, then `RegistryConfig` | **missing** |
| C | `ObservationRegistry` declared inside the `@EnableBatchProcessing` class | recorded |

Measured output (Spring Batch 6.0.4, Java 25):

```
[SCENARIO A: RegistryConfig registered BEFORE the @EnableBatchProcessing class]
  spring.batch meters = [spring.batch.job, spring.batch.job.active, spring.batch.job.launch.count, spring.batch.job.launch.count.active, spring.batch.step, spring.batch.step.active]
  spring.batch.job.launch.count present = true
[SCENARIO B: RegistryConfig registered AFTER the @EnableBatchProcessing class]
  spring.batch meters = [spring.batch.job, spring.batch.job.active, spring.batch.step, spring.batch.step.active]
  spring.batch.job.launch.count present = false
[SCENARIO C: ObservationRegistry declared INSIDE the @EnableBatchProcessing class]
  spring.batch meters = [spring.batch.job, spring.batch.job.active, spring.batch.job.launch.count, spring.batch.job.launch.count.active, spring.batch.step, spring.batch.step.active]
  spring.batch.job.launch.count present = true
```

Declaring the bean inside the `@EnableBatchProcessing` class (scenario C) is the only
placement that guarantees the ordering. Spring Boot's auto-configured `ObservationRegistry`
is registered via deferred import, which always runs after user configuration classes,
so the Boot auto-configuration always hits scenario B's ordering.
