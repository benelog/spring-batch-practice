package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessLogLineMapperTest {
  @Test
  void mapLine() {
    // given
    var line = "2025-07-28 12:14:16,175.242.91.54,benelog";
    var lineMapper = new AccessLogLineMapper(); // <4>

    // when
    AccessLog log = lineMapper.mapLine(line);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2025-07-28T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}