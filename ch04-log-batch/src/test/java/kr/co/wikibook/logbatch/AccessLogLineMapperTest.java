package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessLogLineMapperTest {
  @Test
  void mapLine() {
    // given
    var line = "2023-03-11 12:14:16,175.242.91.54,benelog";
    var lineMapper = new AccessLogLineMapper(); // <4>

    // when
    AccessLog log = lineMapper.mapLine(line);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2023-03-11T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}