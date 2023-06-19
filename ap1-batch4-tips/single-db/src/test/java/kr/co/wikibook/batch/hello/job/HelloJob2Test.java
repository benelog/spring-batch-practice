package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.batch.hello.supports.JobLaunchers;
import kr.co.wikibook.hello.BatchApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BatchApplication.class)
class HelloJob2Test {
  @Test
  void launchJob(@Autowired JobRepository jobRepository, @Autowired Job helloJob2) throws Exception {
    JobLauncherTestUtils testUtils = JobLaunchers.createTestUtils(jobRepository, helloJob2);
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
