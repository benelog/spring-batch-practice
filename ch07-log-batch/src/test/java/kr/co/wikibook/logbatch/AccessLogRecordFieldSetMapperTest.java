package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

public class AccessLogRecordFieldSetMapperTest {

  @Test
  void mapLine() throws Exception {
    // given
    var line = "2025-08-11 12:14:16,175.242.91.54,benelog";
    LineMapper<AccessLog> lineMapper = buildAccessLogLineMapper();

    // when
    AccessLog log = lineMapper.mapLine(line, 1);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2025-08-11T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }

  LineMapper<AccessLog> buildAccessLogLineMapper() {
    var lineMapper = new DefaultLineMapper<AccessLog>();
    var lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setNames("accessDateTime", "ip", "username");
    lineMapper.setLineTokenizer(lineTokenizer);

    Converter<String, Instant> converter = new Converter<>() {
      private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
          "yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

      @Override
      public Instant convert(String source) {
        return Instant.from(FORMATTER.parse(source));
      }
    };
    var conversionService = new DefaultConversionService();
    conversionService.addConverter(converter);

    var fieldSetMapper = new RecordFieldSetMapper<>(AccessLog.class, conversionService);
    lineMapper.setFieldSetMapper(fieldSetMapper);
    return lineMapper;
  }
}
