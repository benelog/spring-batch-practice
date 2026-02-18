package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class HelloParamJobTest {
  @Autowired
  JobOperatorTestUtils testUtils;

  @BeforeEach
  void prepareTestUtils(@Autowired Job helloParamJob) { // <2>
    testUtils.setJob(helloParamJob);
  }

  @Test
  void startJobWithValidParameter() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("helloDate", LocalDate.of(2025, 7, 28))
        .toJobParameters();
    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }

  @Test
  void startJobWithInvalidParameter() {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("goodDate", LocalDate.of(2025, 7, 28))
        .toJobParameters();
    assertThatExceptionOfType(InvalidJobParametersException.class)
        .isThrownBy(() -> testUtils.startJob(params))
        .withMessageContaining("do not contain required keys: [helloDate]");
  }
}
