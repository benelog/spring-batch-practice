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

@SpringBootTest(classes = BatchApplication.class)
@SpringBatchTest
class HelloJobFailedTest {
  // @Test 실패하는 테스트
  void launchJob(@Autowired JobLauncherTestUtils testUtils) throws Exception {
    JobExecution execution = testUtils.launchJob();
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
  }
}
