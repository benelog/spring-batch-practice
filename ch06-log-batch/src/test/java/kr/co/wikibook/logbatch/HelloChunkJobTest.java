package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class HelloChunkJobTest {
  @Test
  void launchJob(
          @Autowired JobLauncherTestUtils testUtils,
          @Autowired @Qualifier(HelloChunkJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLong("chunkSize", 5L)
        .toJobParameters();
    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
