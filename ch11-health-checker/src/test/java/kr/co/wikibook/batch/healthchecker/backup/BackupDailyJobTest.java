package kr.co.wikibook.batch.healthchecker.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class BackupDailyJobTest {
  @TempDir
  Path baseDir;

  @Test
  void execute(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired @Qualifier("backupDailyJob") Job job
  ) throws Exception {
    testUtils.setJob(job);

    Path sourcePath = prepareSourcePath("testDir", "test1.txt");
    Path targetParentPath = this.baseDir.resolve("backup");
    Files.createDirectories(targetParentPath);

    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addString("sourceDirectory", sourcePath.toString())
        .addString("targetParentDirectory", targetParentPath.toString())
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }

  private Path prepareSourcePath(String directoryName, String fileName) throws IOException {
    Path sourcePath = this.baseDir.resolve(directoryName);
    sourcePath.toFile().mkdir();
    Path file = sourcePath.resolve(fileName);
    Files.writeString(file, "test content");
    return sourcePath;
  }
}
