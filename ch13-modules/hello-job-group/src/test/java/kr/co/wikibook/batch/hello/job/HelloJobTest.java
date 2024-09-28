package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.batch.hello.HelloJobGroupRunner;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBatchTest
@SpringJUnitConfig(HelloJobGroupRunner.class)
class HelloJobTest {
  @Test
  void launchJob(
      @Autowired JobLocator jobLocator,
      @Autowired JobLauncherTestUtils testUtils
  ) throws Exception {
    Job job = jobLocator.getJob("helloJob");
    testUtils.setJob(job);
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
