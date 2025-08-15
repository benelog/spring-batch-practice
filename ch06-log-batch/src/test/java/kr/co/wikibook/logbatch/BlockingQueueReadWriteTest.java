package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.queue.BlockingQueueItemReader;
import org.springframework.batch.item.queue.BlockingQueueItemWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BlockingQueueReadWriteTest {
  @Test
  void writeAndRead() throws Exception {
    var queue = new ArrayBlockingQueue<>(1000);
    var writer = new BlockingQueueItemWriter<>(queue);
    writer.write(Chunk.of("a", "b"));

    var reader = new BlockingQueueItemReader<>(queue);
    reader.setTimeout(3, TimeUnit.SECONDS);
    assertThat(reader.read()).isEqualTo("a");
    assertThat(reader.read()).isEqualTo("b");
    assertThat(reader.read()).isNull();
  }
}
