package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.hello.BatchApplication;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = "spring.batch.job.enabled=false",
    classes = BatchApplication.class
)
@SpringBatchTest
class HelloJobFailedTest {

  @Ignore("job이 2개 설정된 상태에서는 실패")
  @Test
  void launchJob(@Autowired JobLauncherTestUtils testUtils) throws Exception {
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
