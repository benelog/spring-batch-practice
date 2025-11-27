package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class CheckStatusJobTest {

  @Autowired
  JobOperatorTestUtils testUtils;

  @BeforeEach
  void prepareTestUtils(@Autowired @Qualifier("checkStatusJob") Job checkStatusJob) {
    testUtils.setJob(checkStatusJob);
  }

  @Test
  void launchCountAccessLogStep() {
    JobExecution jobExecution = testUtils.startStep("countAccessLogStep");

    assertThat(jobExecution.getStatus()).isSameAs(BatchStatus.COMPLETED);
    StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
    long count = stepExecution.getExecutionContext().getLong("count");
    assertThat(count).isGreaterThanOrEqualTo(0L);
    count = jobExecution.getExecutionContext().getLong("count");
    assertThat(count).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void startCheckDiskSpaceStep() {
    JobParameters jobParameters = testUtils.getUniqueJobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 10L)
        .toJobParameters();
    var jobExecutionContext = new ExecutionContext();

    JobExecution execution = testUtils.startStep(
        "checkDiskSpaceStep", jobParameters, jobExecutionContext
    );
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }

  @Test
  void startLogDiskSpaceStep() {
    JobParameters jobParameters = testUtils.getUniqueJobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 10L)
        .toJobParameters();
    var jobExecutionContext = new ExecutionContext();
    jobExecutionContext.putLong("usablePercentage", 50L);

    JobExecution execution = testUtils.startStep(
        "logDiskSpaceStep", jobParameters, jobExecutionContext
    );
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
