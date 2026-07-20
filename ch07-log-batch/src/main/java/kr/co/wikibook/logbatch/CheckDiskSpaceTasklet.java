package kr.co.wikibook.logbatch;

import java.util.Map;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class CheckDiskSpaceTasklet implements Tasklet {

  private final SpaceChecker spaceChecker = new SpaceChecker();

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    String directory = (String) jobParameters.get("directory");
    long minUsablePercentage = (long) jobParameters.get("minUsablePercentage");

    int usablePercentage = spaceChecker.run(directory, (int) minUsablePercentage);

    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
    jobExecutionContext.putLong("usablePercentage", usablePercentage);

    return RepeatStatus.FINISHED;
  }
}
