package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;

class AccessLogCsvReaderTest {

  Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  void read() throws Exception {
    // given
    var jobConfig = new AccessLogJobConfig(null, null, Path.of("src/test/resources"));
    FlatFileItemReader<AccessLog> reader = jobConfig.accessLogCsvReader(LocalDate.of(2025, 7, 28));

    // when
    reader.open(new ExecutionContext());
    int itemCount = 0;
    AccessLog item;
    while ((item = reader.read()) != null) {
      itemCount++;
      logger.debug("{}", item);
    }
    reader.close();

    // then
    assertThat(itemCount).isEqualTo(3);
  }

  @Test
  void instanceOfItemStream() {
    var config = new AccessLogJobConfig(null, null, Path.of("src/test/resources"));
    ItemReader<AccessLog> accessLogCsvReader = config.accessLogCsvReader(null);
    assertThat(accessLogCsvReader).isInstanceOf(ItemStream.class);
  }
}