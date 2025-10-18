package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

class AccessLogCsvReaderTest {
  Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  void read() throws IOException {
    // given
    var resource = new ClassPathResource("2025-07-28.csv"); // <1>
    var reader = new AccessLogCsvReader(resource);

    // when
    reader.open(new ExecutionContext());
    int itemCount = 0;
    AccessLog item;
    while ((item = reader.read()) != null) {
      itemCount++;
      logger.info("{}", item);
    }
    reader.close();

    // then
    assertThat(itemCount).isEqualTo(3);
  }

  @Test
  void instanceOfItemStream() {
    var config = new AccessLogJobConfig(null, null, Path.of("."));
    ItemReader<AccessLog> accessLogCsvReader = config.accessLogCsvReader(null);
    assertThat(accessLogCsvReader).isInstanceOf(ItemStream.class);
  }
}