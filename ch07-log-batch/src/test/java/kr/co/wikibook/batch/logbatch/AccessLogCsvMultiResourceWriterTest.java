package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
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
    var item = new AccessLog(Instant.parse("2023-12-10T11:14:16Z"), "127.0.0.1", "benelog");

    // when
    writer.open(new ExecutionContext());
    writer.write(new Chunk<>(List.of(item)));
    writer.write(new Chunk<>(List.of(item)));
    writer.close();

    // then
    assertThat(tempPath.resolve("access-log.txt.1")).exists();
    assertThat(tempPath.resolve("access-log.txt.2")).exists();
  }
}
