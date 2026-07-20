package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

class CheckDiskSpaceTaskletTest {

  @DisplayName("디스크 용량이 기대치보다 많으면 남은 용량이 기록된다")
  @Test
  void checkDiskSpaceWhenSufficient() {
    // given
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 1L)
        .toJobParameters();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    var tasklet = new CheckDiskSpaceTasklet();

    // when
    tasklet.execute(stepContribution, chunkContext);

    // then
    ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
    long usablePercentage = jobExecutionContext.getLong("usablePercentage"); // <1>
    assertThat(usablePercentage).isGreaterThan(0L);
  }

  @DisplayName("디스크 용량이 기대치보다 적으면 예외가 발생한다")
  @Test
  void checkDiskSpaceWhenInsufficient() {
    // given
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 100L)
        .toJobParameters();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    var tasklet = new CheckDiskSpaceTasklet();

    // when, then
    assertThatThrownBy(() ->
        tasklet.execute(stepContribution, chunkContext)
    ).isInstanceOf(IllegalStateException.class);
  }
}
