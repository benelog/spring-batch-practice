package kr.co.wikibook.hello.supports;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.core.task.SyncTaskExecutor;

public class JobLaunchers {
  public static JobLauncherTestUtils createTestUtils( JobRepository jobRepository, Job job) {
    var testUtils = new JobLauncherTestUtils();
    testUtils.setJobRepository(jobRepository);
    testUtils.setJob(job);

    var jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(new SyncTaskExecutor());
    testUtils.setJobLauncher(jobLauncher);

    return testUtils;
  }
}
