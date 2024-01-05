package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.core.io.ClassPathResource;

class AccessLogJsonReaderTest {
  @Test
  void read() throws Exception {
    var resource = new ClassPathResource("sample-access-log.json");
    JsonItemReader<AccessLog> reader = JsonComponents.buildJsonItemReader(resource);
    reader.open(new ExecutionContext());AccessLog item = reader.read();
    reader.close();

    assertThat(item.accessDateTime()).isEqualTo("2023-12-10T11:14:16Z");
    assertThat(item.ip()).isEqualTo("175.242.91.54");
    assertThat(item.username()).isEqualTo("benelog");
  }
}
