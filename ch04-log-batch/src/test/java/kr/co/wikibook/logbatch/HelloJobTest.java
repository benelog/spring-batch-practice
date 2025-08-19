package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
    LogBatchApplication.class,
    TestJobRepositoryConfig.class
})
@ActiveProfiles("test")
class HelloJobTest {
  JobLauncherTestUtils testUtils = new JobLauncherTestUtils();

  @BeforeEach
  void setUp(
      @Autowired JobRepository jobRepository,
      @Autowired JobLauncher jobLauncher,
      @Autowired @Qualifier("helloJob") Job helloJob
  ) {
    this.testUtils.setJobRepository(jobRepository);
    this.testUtils.setJobLauncher(jobLauncher);
    this.testUtils.setJob(helloJob);
  }

  @Test
  void launchJob() throws Exception {
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
