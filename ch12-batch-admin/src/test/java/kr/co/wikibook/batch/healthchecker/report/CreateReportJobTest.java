package kr.co.wikibook.batch.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
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
class CreateReportJobTest {

  JobLauncherTestUtils testUtils;

  @BeforeEach
  void setUp(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier("createReportJob") Job job) {
    testUtils.setJob(job);
    this.testUtils = testUtils;
  }

  @Test
  void launchJobForDailyReport() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2024, 3, 13))
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }

  @Test
  void launchJobForWeeklyReport() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2024, 3, 18))
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }

  @Test
  void launchJobForMonthlyReport() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2024, 4, 1))
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
