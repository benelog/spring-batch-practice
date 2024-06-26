package kr.co.wikibook.batch.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class CreateAndSendReportJobTest {

  JobLauncherTestUtils testUtils;

  @BeforeEach
  void setUp(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier("createAndSendReportJob") Job job) {
    testUtils.setJob(job);
    this.testUtils = testUtils;
  }

  @Test
  void launchJobOnWorkday() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2024, 5, 1))
        .addLong("runId", Instant.now().toEpochMilli())
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }

  @Test
  void launchJobOnHoliday() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2024, 5, 5))
        .addLong("runId", Instant.now().toEpochMilli())
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }
}
