package kr.co.wikibook.healthchecker;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
public class BatchAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchAdminApplication.class, args);
	}

	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepository) {
		var launcher = new TaskExecutorJobLauncher();
		launcher.setJobRepository(jobRepository);
		launcher.setTaskExecutor(taskExecutor());
		return launcher;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		var taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(10);
		return taskExecutor;
	}

}
