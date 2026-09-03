# Probe: chunk scan mode in Spring Batch 6.0.6 (GH-5490, GH-5493)

Spring Boot 4.1.2-SNAPSHOT / Spring Batch 6.0.6-SNAPSHOT (override with `-DbatchVersion=6.0.5`), Java 21+.

Records what `ChunkOrientedStep` does while scanning a chunk after a skippable write failure:
how many times the `ItemProcessor` is invoked, and which `ChunkListener` callbacks fire with which item types.

- https://github.com/spring-projects/spring-batch/issues/5490 — processor is re-invoked during scan again (as in v5); `processorNonTransactional()` re-added to `ChunkOrientedStepBuilder`.
- https://github.com/spring-projects/spring-batch/issues/5493 — `beforeChunk` receives input items, `onChunkError` fires for the failed chunk and per faulty scanned item, no callbacks in concurrent steps.

```bash
./gradlew bootRun                        # 6.0.6-SNAPSHOT
./gradlew bootRun -DbatchVersion=6.0.5   # comment out the processorNonTransactional() line first: it does not exist in 6.0.5
```

Step `<String, Integer>`, chunk size 3, writer throws for items 2 and 3, `skip(ItemInvalid.class)`.

## 6.0.6-SNAPSHOT (2026-09-04)

```text
[PROBE] default         status=COMPLETED read=3 write=1 writeSkip=2 rollback=3 processCalls=6
[PROBE]     beforeChunk(String:1 String:2 String:3)
[PROBE]     onChunkError(Integer:1 Integer:2 Integer:3)
[PROBE]     beforeChunk(String:1)
[PROBE]     afterChunk(Integer:1)
[PROBE]     beforeChunk(String:2)
[PROBE]     onSkipInWrite(2, ItemInvalid)
[PROBE]     onChunkError(Integer:2)
[PROBE]     beforeChunk(String:3)
[PROBE]     onSkipInWrite(3, ItemInvalid)
[PROBE]     onChunkError(Integer:3)
[PROBE] nonTransactional status=COMPLETED read=3 write=1 writeSkip=2 rollback=3 processCalls=3
        (same callbacks as above)
[PROBE] concurrent      status=COMPLETED read=3 write=1 writeSkip=2 rollback=3 processCalls=6
[PROBE]     onSkipInWrite(2, ItemInvalid)
[PROBE]     onSkipInWrite(3, ItemInvalid)
```

## 6.0.5

```text
[PROBE] default         status=COMPLETED read=3 write=1 writeSkip=2 rollback=3 processCalls=3
[PROBE]     beforeChunk(String:1 String:2 String:3)
[PROBE]     beforeChunk(Integer:1)
[PROBE]     afterChunk(Integer:1)
[PROBE]     beforeChunk(Integer:2)
[PROBE]     onSkipInWrite(2, ItemInvalid)
[PROBE]     beforeChunk(Integer:3)
[PROBE]     onSkipInWrite(3, ItemInvalid)
[PROBE] concurrent      status=COMPLETED read=3 write=1 writeSkip=2 rollback=3 processCalls=3
[PROBE]     beforeChunk(Integer:1)
[PROBE]     afterChunk(Integer:1)
[PROBE]     beforeChunk(Integer:2)
[PROBE]     onSkipInWrite(2, ItemInvalid)
[PROBE]     beforeChunk(Integer:3)
[PROBE]     onSkipInWrite(3, ItemInvalid)
```
