package kr.co.wikibook.hello;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import kr.co.wikibook.hello.supports.JobLaunchers;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HelloParamsJobTest {

  @Test
  void launchJob(@Autowired JobRepository jobRepository, @Autowired Job helloParamsJob) throws Exception {
    JobLauncherTestUtils testUtils = JobLaunchers.createTestUtils(jobRepository, helloParamsJob);

    Date helloDate = Date.from(Instant.parse("2023-06-10T00:00:00Z"));
    JobParameters parameters = testUtils.getUniqueJobParametersBuilder()
        .addDate("helloDate", helloDate)
        .addString("helloLocalDate", "2023.06.15")
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(parameters);
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
