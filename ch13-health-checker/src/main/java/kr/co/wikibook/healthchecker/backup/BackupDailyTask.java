package kr.co.wikibook.healthchecker.backup;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

public class BackupDailyTask implements Callable<RepeatStatus> {

  private final Logger logger = LoggerFactory.getLogger(BackupDailyTask.class);
  private final BackupRoute route;
  private final Clock clock;

  public BackupDailyTask(BackupRoute route, Clock clock) {
    this.route = route;
    this.clock = clock;
  }

  @Override
  public RepeatStatus call() throws IOException {
    LocalDate today = LocalDate.now(clock);
    Path sourceDirectory = route.getSourceDirectory();
    String targetDirectoryName = sourceDirectory.getFileName() + "_" + today;
    Path targetDirectory = route.getTargetParentDirectory().resolve(targetDirectoryName);
    targetDirectory.toFile().mkdir();

    FileSystemUtils.copyRecursively(sourceDirectory, targetDirectory);
    logger.info("Backup completed from {} to {}", sourceDirectory, targetDirectory);
    return RepeatStatus.FINISHED;
  }
}
