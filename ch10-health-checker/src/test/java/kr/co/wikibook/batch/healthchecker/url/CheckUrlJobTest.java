package kr.co.wikibook.batch.healthchecker.url;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class CheckUrlJobTest {

  @Test
  void execute(
      @Autowired JobLauncherTestUtils testUtils,
      @Autowired Job checkUrlJob
  ) throws Exception {
    testUtils.setJob(checkUrlJob);
    var urls = new ClassPathResource("ok-urls.txt");
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addString(CheckUrlJobConfig.INPUT_FILE_PARAM, urls.getFile().getPath())
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(params);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    Path outputFile = Path.of(CheckUrlJobConfig.OUTPUT_FILE_PATH);
    assertThat(outputFile).isNotEmptyFile();
  }
}
