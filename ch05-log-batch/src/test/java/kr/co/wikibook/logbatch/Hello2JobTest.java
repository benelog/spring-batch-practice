package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest({
    "spring.batch.job.enabled=false",
    "spring.batch.job.name=" + Hello2JobConfig.JOB_NAME
})
@SpringBatchTest
class Hello2JobTest {
  @Test
  void launchJob(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired Job hello2Job
  ) throws Exception {
    testUtils.setJob(hello2Job);
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
