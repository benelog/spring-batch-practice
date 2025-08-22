package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.LineMapper;

class FixLengthLineMapperTest {
  @Test
  void mapLine() throws Exception {
    var line = "2025-07-20 12:14:16 175.242.91.54   benelog   ";
    LineMapper<AccessLog> lineMapper = LineMapperComponents.buildAccessLogLineMapper();

    AccessLog log = lineMapper.mapLine(line, 0);

    assertThat(log.accessDateTime()).isEqualTo("2025-07-20T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}
