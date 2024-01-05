package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
public class AccessLogJobTest {
  @Test
  void launchJob(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier(AccessLogJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addString("accessLog", "classpath:/sample-access-log.csv")
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);

    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
    Collection<StepExecution> stepExecutions = execution.getStepExecutions();
    StepExecution firstStep = stepExecutions.iterator().next();
    assertThat(firstStep.getWriteCount()).isEqualTo(3);
  }
}
