package kr.co.wikibook.batch.healthchecker.backup;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

public class DeleteOldDirectoriesTask implements Callable<RepeatStatus> {

  private final Logger logger = LoggerFactory.getLogger(DeleteOldDirectoriesTask.class);
  private final Path parentDirectory;
  private final int daysOfKeeping;
  private final Clock clock;

  public DeleteOldDirectoriesTask(Path parentDirectory, int daysOfKeeping, Clock clock) {
    this.parentDirectory = parentDirectory;
    this.daysOfKeeping = daysOfKeeping;
    this.clock = clock;
  }

  @Override
  public RepeatStatus call() {
    Instant now = this.clock.instant();
    Instant baseInstant = now.minus(daysOfKeeping, ChronoUnit.DAYS);
    long baseEpochMilli = baseInstant.toEpochMilli();

    File[] files = parentDirectory.toFile().listFiles();
    for (File file : files) {
      if (!file.isDirectory()) {
        continue;
      }
      if (file.lastModified() < baseEpochMilli) {
        FileSystemUtils.deleteRecursively(file);
        logger.info("Deleted : {}", file);
      }
    }
    return RepeatStatus.FINISHED;
  }
}
