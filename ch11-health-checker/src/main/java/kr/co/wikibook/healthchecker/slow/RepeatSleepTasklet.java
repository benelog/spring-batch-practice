package kr.co.wikibook.healthchecker.slow;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class RepeatSleepTasklet implements Tasklet {

  private final Logger log = LoggerFactory.getLogger(RepeatSleepTasklet.class);
  private int count = 0;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
    count++;
    log.info("repeat count : {}", count);

    StepExecution stepExecution = contribution.getStepExecution();
    JobParameters jobParameters = stepExecution.getJobParameters();
    long limit = jobParameters.getLong("limit", 1L);
    if  (count > limit) {
      stepExecution.setTerminateOnly();
    }
    return RepeatStatus.CONTINUABLE;
  }
}
