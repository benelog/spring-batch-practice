package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class HelloChunkJobTest {
  @Test
  void launchJob(
          @Autowired JobOperatorTestUtils testUtils,
          @Autowired @Qualifier(HelloChunkJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLong("chunkSize", 5L)
        .toJobParameters();
    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }

  @Test
  void launchJob2(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier(HelloChunkJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLong("chunkSize", 5L)
        .toJobParameters();
    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
