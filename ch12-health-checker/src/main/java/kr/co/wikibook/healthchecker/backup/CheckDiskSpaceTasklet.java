package kr.co.wikibook.healthchecker.backup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class CheckDiskSpaceTasklet implements Tasklet {
  private final BackupRoute route;

  public CheckDiskSpaceTasklet(BackupRoute route) {
    this.route = route;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws IOException {
    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
    ExecutionContext executionContext = jobExecution.getExecutionContext();

    long sourceSize = getDirectorySize(route.getSourceDirectory());
    executionContext.putLong("sourceSize", sourceSize);
    long usableSpace = route.getTargetParentDirectory().toFile().getUsableSpace();
    executionContext.putLong("usableSpace", usableSpace);
    int executionCount = executionContext.getInt("executionCount", 0);
    executionCount++;
    executionContext.putInt("executionCount", executionCount);

    return RepeatStatus.FINISHED;
  }

  private long getDirectorySize(Path directory) throws IOException {
    return Files.walk(directory)
        .map(Path::toFile)
        .filter(File::isFile)
        .mapToLong(File::length)
        .sum();
  }
}
