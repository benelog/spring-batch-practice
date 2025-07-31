package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class HelloParamJobTest {
  @Autowired
  JobLauncherTestUtils testUtils;

  @BeforeEach
  void prepareTestUtils(@Autowired @Qualifier("helloParamJob") Job helloParamJob) {
    testUtils.setJob(helloParamJob);
  }

  @Test
  void launchJob() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("helloDate", LocalDate.of(2025, 7, 28))
        .toJobParameters();
    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }

  @Test
  void launchJobWithoutJobParameters() {
    assertThatExceptionOfType(JobParametersInvalidException.class)
        .isThrownBy(() -> testUtils.launchJob())
        .withMessageContaining("do not contain required keys: [helloDate]");
  }

  @Test
  void launchJobWithRunId(@Autowired JobExplorer jobExplorer) throws Exception {
    JobParameters params = new JobParametersBuilder(jobExplorer)
        .getNextJobParameters(testUtils.getJob())
        .addLocalDate("helloDate", LocalDate.of(2025, 7, 28))
        .toJobParameters();

    assertThat(params.getLong("runId")).isNotNull();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
