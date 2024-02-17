package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.LineMapper;

class FixLengthLineMapperTest {
  @Test
  void mapLine() throws Exception {
    var line = "2023-12-20 12:14:16 175.242.91.54   benelog   ";
    LineMapper<AccessLog> lineMapper = LineMapperComponents.buildAccessLogLineMapper();

    AccessLog log = lineMapper.mapLine(line, 0);

    assertThat(log.accessDateTime()).isEqualTo("2023-12-20T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}
