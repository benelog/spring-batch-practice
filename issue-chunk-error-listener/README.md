# Reproducer: `@OnChunkError` is never invoked on a chunk-oriented step

Spring Boot 4.1.0 / Spring Batch 6.0.4, Java 17+.

## Bug description

`ChunkOrientedStepBuilder#listener(Object)` scans for `@OnChunkError` and builds a listener
proxy for it, but `StepListenerMetaData` has no entry for that annotation. The proxy is
registered and the callback is never wired, so the method is silently skipped. No warning is
logged.

`@BeforeChunk` and `@AfterChunk` on the same class work, which makes the gap easy to miss.

## Steps to reproduce

```bash
./gradlew bootRun
```

Three jobs run. Each has a chunk-oriented step whose writer throws on the second chunk.

```java
public class AnnotatedChunkListener {

	@BeforeChunk
	public void beforeChunk(Chunk<Integer> chunk) { ... }

	@AfterChunk
	public void afterChunk(Chunk<Integer> chunk) { ... }

	@OnChunkError                                       // never called
	public void onChunkError(Exception exception, Chunk<Integer> chunk) { ... }
}
```

### Actual output

```text
[DIAG] ChunkListener interface    = [interface:beforeChunk, interface:afterChunk, interface:beforeChunk, interface:onChunkError]
[DIAG] @OnChunkError              = [annotation:beforeChunk, annotation:afterChunk, annotation:beforeChunk]
[DIAG] @AfterChunkError (5.x)     = []
```

### Expected output

```text
[DIAG] ChunkListener interface    = [interface:beforeChunk, interface:afterChunk, interface:beforeChunk, interface:onChunkError]
[DIAG] @OnChunkError              = [annotation:beforeChunk, annotation:afterChunk, annotation:beforeChunk, annotation:onChunkError]
```

Also asserted as tests:

```bash
./gradlew test
```

`interfaceCallbackIsInvoked` and `otherChunkAnnotationsAreInvoked` pass;
`onChunkErrorAnnotationShouldBeInvoked` and `afterChunkErrorAnnotationShouldBeInvoked` fail.

## Root cause

`StepListenerMetaData` (6.0.4) has no `OnChunkError` constant. `AFTER_CHUNK_ERROR` still maps
to the deprecated `ChunkListener#afterChunkError(ChunkContext)`:

```java
BEFORE_CHUNK("beforeChunk", "before-chunk-method", BeforeChunk.class, ChunkListener.class, Chunk.class),
AFTER_CHUNK("afterChunk", "after-chunk-method", AfterChunk.class, ChunkListener.class, Chunk.class),
AFTER_CHUNK_ERROR("afterChunkError", "after-chunk-error-method", AfterChunkError.class, ChunkListener.class,
		ChunkContext.class),
// no entry for OnChunkError
```

`AbstractListenerFactoryBean#getObject` iterates over these constants, so the proxy it returns
has nothing bound to `onChunkError(Exception, Chunk)`. `ChunkOrientedStep` calls only
`onChunkError(Exception, Chunk)`; the deprecated `afterChunkError(ChunkContext)` is called by
`TaskletStep` alone.

`@BeforeChunk`/`@AfterChunk` were migrated to the `Chunk` signature in #4961, and `@OnChunkError`
was added in 6.0 with a javadoc that documents `void onChunkError(Exception, Chunk)`. The
metadata entry appears to have been left out of both changes.

`@AfterChunkError` is a separate, smaller gap: `ChunkOrientedStepBuilder#listener(Object)` does
not scan for it at all, so a class carrying only that annotation registers nothing.

## Suggested fix

Add the missing constant and use it wherever `AFTER_CHUNK_ERROR` is used for chunk-oriented
steps:

```java
ON_CHUNK_ERROR("onChunkError", "on-chunk-error-method", OnChunkError.class, ChunkListener.class,
		Exception.class, Chunk.class),
```

## Impact

An annotation-based chunk error handler compiles, registers without complaint, and does
nothing. Error notifications, cleanup, and failure counters written this way are silently
lost, and the step still reports the failure normally, so nothing points at the listener.

## Workaround

Implement `ChunkListener` and override `onChunkError(Exception, Chunk)`.

## Related

- #4961 / #4969 — `@BeforeChunk` / `@AfterChunk` fixed to accept `Chunk` as documented.
  `AFTER_CHUNK_ERROR` was not migrated in that change.
- #5297 — `@AfterChunkError` with a `ChunkContext` parameter on `SimpleStepBuilder`. Different
  path: that one throws `IllegalArgumentException`, this one fails silently.
- #5226 — `ChunkListener` callbacks on an `ItemReader` not invoked in 6.x.
