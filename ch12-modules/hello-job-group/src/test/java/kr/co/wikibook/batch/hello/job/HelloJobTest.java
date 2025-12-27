package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.batch.hello.HelloJobGroupRunner;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBatchTest
@SpringJUnitConfig(HelloJobGroupRunner.class)
class HelloJobTest {
  @Test
  void launchJob(
      @Autowired JobRegistry registry,
      @Autowired JobOperatorTestUtils testUtils
  ) throws Exception {
    Job job = registry.getJob("helloJob");
    testUtils.setJob(job);
    JobExecution execution = testUtils.startJob();
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
