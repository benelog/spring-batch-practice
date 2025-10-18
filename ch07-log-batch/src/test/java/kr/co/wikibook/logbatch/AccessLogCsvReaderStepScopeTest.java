package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
class AccessLogCsvReaderStepScopeTest {

  Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  void read(@Autowired FlatFileItemReader<AccessLog> reader) throws Exception {
    JobParameters params = new JobParametersBuilder()
        .addLocalDate("date", LocalDate.of(2025, 7, 28))
        .toJobParameters();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(params);

    StepScopeTestUtils.doInStepScope(stepExecution, () -> {
      reader.open(new ExecutionContext());
      int itemCount = 0;
      AccessLog item;
      while ((item = reader.read()) != null) {
        itemCount++;
        logger.info("{}", item);
      }
      reader.close();
      assertThat(itemCount).isEqualTo(3);
      return null;
    });
  }
}
