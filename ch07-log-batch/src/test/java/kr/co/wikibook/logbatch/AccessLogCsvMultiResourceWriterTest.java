package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.core.io.PathResource;

public class AccessLogCsvMultiResourceWriterTest {
  @Test
  void writeMulti(@TempDir Path tempPath) throws Exception {
    // given
    Path outputPath = tempPath.resolve("access-log.txt");
    var resource = new PathResource(outputPath);
    MultiResourceItemWriter<AccessLog> writer = AccessLogComponents.buildMultiResourceItemWriter(resource, 1);
    var item = new AccessLog(Instant.parse("2025-07-28T11:14:16Z"), "127.0.0.1", "benelog");

    // when
    writer.open(new ExecutionContext());
    writer.write(Chunk.of(item));
    writer.write(Chunk.of(item));
    writer.close();

    // then
    assertThat(tempPath.resolve("access-log.txt.1")).exists();
    assertThat(tempPath.resolve("access-log.txt.2")).exists();
  }
}
