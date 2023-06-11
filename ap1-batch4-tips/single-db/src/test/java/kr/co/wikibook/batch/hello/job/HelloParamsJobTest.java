package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
class HelloParamsJobTest {

  JobLauncherTestUtils testUtils = new JobLauncherTestUtils();

  @BeforeEach
  void setUp(
      @Autowired JobRepository jobRepository,
      @Autowired JobLauncher jobLauncher,
      @Autowired Job helloParamsJob
  ) {
    this.testUtils.setJobRepository(jobRepository);
    this.testUtils.setJobLauncher(jobLauncher);
    this.testUtils.setJob(helloParamsJob);
  }

  @Test
  void launchJob() throws Exception {
    Date helloDate = Date.from(Instant.parse("2023-06-10T00:00:00Z"));
    JobParameters parameters = this.testUtils.getUniqueJobParametersBuilder()
        .addDate("helloDate", helloDate)
        .addString("helloLocalDate", "2023.06.15")
        .toJobParameters();

    JobExecution execution = this.testUtils.launchJob(parameters);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
