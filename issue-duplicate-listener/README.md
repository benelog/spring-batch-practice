# Reproducer: listener callbacks fire twice when an item handler mixes an interface and an annotation

Spring Boot 4.1.0 / Spring Batch 6.0.4, Java 17+.

If an `ItemReader`, `ItemProcessor`, or `ItemWriter`

1. implements a `StepListener` sub-interface (here `ItemProcessListener`), and
2. has at least one method annotated with a listener annotation (here `@BeforeStep`),

then the object is registered as a listener **twice**, so every interface callback is
invoked twice per item. Adding a single `@BeforeStep` method to an existing
listener-implementing processor silently doubles every `beforeProcess` / `afterProcess` /
`onProcessError` call. Spring Batch 5.2.x registers the same class once.

## Run

```bash
./gradlew bootRun
```

Two identical jobs run. `plainJob` uses a processor that only implements
`ItemProcessListener`; `annotatedJob` uses the same processor plus one `@BeforeStep` method.

### Actual output

```text
[DIAG] without a listener annotation = [beforeProcess:1, afterProcess:1, beforeProcess:2, afterProcess:2]
[DIAG] with @BeforeStep added        = [beforeStep, beforeProcess:1, beforeProcess:1, afterProcess:1, afterProcess:1, beforeProcess:2, beforeProcess:2, afterProcess:2, afterProcess:2]
```

### Expected output

```text
[DIAG] without a listener annotation = [beforeProcess:1, afterProcess:1, beforeProcess:2, afterProcess:2]
[DIAG] with @BeforeStep added        = [beforeStep, beforeProcess:1, afterProcess:1, beforeProcess:2, afterProcess:2]
```

`@BeforeStep` itself is invoked only once. Only the interface callbacks are doubled.

The same thing is asserted as a test:

```bash
./gradlew test
```

`callbacksFireOnceWhenOnlyTheInterfaceIsImplemented` passes and
`callbacksShouldAlsoFireOnceWhenAListenerAnnotationIsAdded` fails.

## Root cause

`ChunkOrientedStepBuilder#addAsStreamAndListener` runs two independent checks and adds a
listener in each branch:

```java
private void addAsStreamAndListener(Object itemHandler) {
    // Register as stream if applicable
    if (itemHandler instanceof ItemStream itemStream) {
        this.streams.add(itemStream);
    }
    // Register as listener if implements the interface
    if (itemHandler instanceof StepListener listener) {
        this.stepListeners.add(listener);            // (1)
    }
    // Register as listener if annotated methods are present
    if (StepListenerFactoryBean.isListener(itemHandler)) {
        StepListener listener = StepListenerFactoryBean.getListener(itemHandler);
        this.stepListeners.add(listener);            // (2)
    }
}
```

The branches are not mutually exclusive.

- `(1)` adds the raw object.
- `(2)` adds a `StepListenerFactoryBean` proxy. `AbstractListenerFactoryBean#getObject`
  resolves each `StepListenerMetaData` entry **by interface as well as by annotation**, so
  that proxy also implements `ItemProcessListener` and delegates `beforeProcess` /
  `afterProcess` back to the same target.

`build()` then calls `registerTypedListener(...)` for every entry in `stepListeners`, and the
step ends up with two `ItemProcessListener` registrations pointing at the same object.

Spring Batch 5.2.x did this in a single branch, so only the proxy was registered:

```java
// SimpleStepBuilder#registerAsStreamsAndListeners (5.2.2)
for (Object itemHandler : new Object[] { itemReader, itemWriter, itemProcessor }) {
    if (itemHandler instanceof ItemStream) {
        stream((ItemStream) itemHandler);
    }
    if (StepListenerFactoryBean.isListener(itemHandler)) {
        StepListener listener = StepListenerFactoryBean.getListener(itemHandler);
        // ...
    }
}
```

## Impact

A logging-only listener just emits duplicate lines. A callback that accumulates state is
silently wrong: counters double, and a listener that writes skipped or slow items to a file
records every item twice. Nothing fails, so the defect survives a green test suite.

## Suggested fix

Make the two branches mutually exclusive, as in 5.2.x:

```java
if (StepListenerFactoryBean.isListener(itemHandler)) {
    this.stepListeners.add(StepListenerFactoryBean.getListener(itemHandler));
}
else if (itemHandler instanceof StepListener listener) {
    this.stepListeners.add(listener);
}
```

`StepListenerFactoryBean.isListener` already returns `true` for an object that only
implements the interfaces, and in that case `getObject()` returns the delegate unchanged, so
one branch covers both.

## Workaround

Do not mix a listener annotation and a listener interface in one reader/processor/writer.
Move the annotated method into a separate listener object registered with `listener(...)`
(before `chunk()` for `@BeforeStep` / `@AfterStep`), or convert it to the matching interface
method.

## Related

- https://github.com/spring-projects/spring-batch/issues/5451 — `@BeforeStep` / `@AfterStep`
  ignored by `ChunkOrientedStepBuilder#listener(Object)`. Different code path (explicit
  registration after `chunk()`), same class.
- https://github.com/spring-projects/spring-batch/issues/5226 — `ChunkListener` callbacks on
  an `ItemReader` not invoked in 6.x. Closed for 6.0.2.
