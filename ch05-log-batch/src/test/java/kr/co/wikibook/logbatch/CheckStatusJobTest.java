package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class CheckStatusJobTest {

  @Autowired
  JobLauncherTestUtils testUtils;

  @BeforeEach
  void prepareTestUtils(@Autowired @Qualifier("checkStatusJob") Job checkStatusJob) {
    testUtils.setJob(checkStatusJob);
  }

  @Test
  void launchCountAccessLogStep() {
    JobExecution jobExecution = testUtils.launchStep("countAccessLogStep");

    assertThat(jobExecution.getStatus()).isSameAs(BatchStatus.COMPLETED);
    StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
    long count = stepExecution.getExecutionContext().getLong("count");
    assertThat(count).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void launchCheckDiskSpaceStep() {
    JobParameters jobParameters = testUtils.getUniqueJobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 10L)
        .toJobParameters();

    JobExecution execution = testUtils.launchStep("checkDiskSpaceStep", jobParameters);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }

  @Test
  void launchLogDiskSpaceStep() {
    var jobExecutionContext = new ExecutionContext();
    jobExecutionContext.putLong("usablePercentage", 50L);
    JobParameters jobParameters = testUtils.getUniqueJobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 10L)
        .toJobParameters();

    JobExecution execution = testUtils.launchStep("logDiskSpaceStep",
        jobParameters, jobExecutionContext);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
