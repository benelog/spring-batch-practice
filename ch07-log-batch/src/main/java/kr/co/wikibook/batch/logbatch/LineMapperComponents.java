package kr.co.wikibook.batch.logbatch;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class LineMapperComponents {
  public static LineMapper<AccessLog> buildAccessLogLineMapper() {
    FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
    tokenizer.setColumns( // <1>
        new Range(1, 20), // 예: "2020-06-20T12:14:16Z"
        new Range(21, 36), // 예: "175.242.91.54  "
        new Range(37, 46)  // 예: "benelog   "
    );

    var lineMapper = new DefaultLineMapper<AccessLog>();
    lineMapper.setLineTokenizer(tokenizer);
    lineMapper.setFieldSetMapper(new AccessLogFieldSetMapper());
    return lineMapper;
  }
}
