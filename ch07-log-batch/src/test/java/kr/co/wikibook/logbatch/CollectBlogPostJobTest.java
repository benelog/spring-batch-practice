package kr.co.wikibook.logbatch;

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
class CollectBlogPostJobTest {
  @Test
  void launchJob(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier(CollectBlogPostJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}