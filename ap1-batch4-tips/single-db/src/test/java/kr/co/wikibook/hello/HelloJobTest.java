package kr.co.wikibook.hello;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.hello.BatchApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
class HelloJobTest {
  @Test
  void launchJob(
      @Autowired JobRepository jobRepository,
      @Autowired JobLauncher jobLauncher,
      @Autowired Job helloJob
  ) throws Exception {
    JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
    testUtils.setJobRepository(jobRepository);
    testUtils.setJobLauncher(jobLauncher);
    testUtils.setJob(helloJob);

    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
