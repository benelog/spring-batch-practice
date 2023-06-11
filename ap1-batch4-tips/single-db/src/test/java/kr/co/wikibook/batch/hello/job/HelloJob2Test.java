package kr.co.wikibook.batch.hello.job;

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
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
class HelloJob2Test {

	JobLauncherTestUtils testUtils = new JobLauncherTestUtils();

	@BeforeEach
	void setUp(
		@Autowired JobRepository jobRepository,
		@Autowired JobLauncher jobLauncher,
		@Autowired Job helloJob2
	) {
		this.testUtils.setJobRepository(jobRepository);
		this.testUtils.setJobLauncher(jobLauncher);
		this.testUtils.setJob(helloJob2);
	}

	@Test
	void launchJob() throws Exception {
		JobExecution execution = testUtils.launchJob();
		assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
	}
}
