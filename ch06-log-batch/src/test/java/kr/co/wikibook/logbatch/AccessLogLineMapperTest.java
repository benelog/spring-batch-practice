package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

class AccessLogLineMapperTest {
  @Test
  void mapLine() throws Exception {
    // given
    var line = "2023-03-11 12:14:16,175.242.91.54,benelog";
    var jobConfig = new AccessLogJobConfig(null, null);
    LineMapper<AccessLog> lineMapper = buildAccessLogLineMapper();

    // when
    AccessLog log = lineMapper.mapLine(line, 1);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2023-03-11T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }


  LineMapper<AccessLog> buildAccessLogLineMapper() {
    var lineMapper = new DefaultLineMapper<AccessLog>();
    lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
    lineMapper.setFieldSetMapper(new AccessLogFieldSetMapper());
    return lineMapper;
  }
}