package kr.co.wikibook.batch.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class UserRankingJobTest {
  @Test
  void execute(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier("userRankingJob") Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }
}
