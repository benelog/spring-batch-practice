package com.example.duplicatelistener;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class IssueDuplicateListenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueDuplicateListenerApplication.class, args);
	}

	@Bean
	@Profile("!test")
	public CommandLineRunner diagnostics(JobOperator jobOperator, Job annotatedJob, Job plainJob) {
		return args -> {
			jobOperator.start(plainJob, new JobParameters());
			System.out.println("[DIAG] without a listener annotation = " + CallLog.drain());

			jobOperator.start(annotatedJob, new JobParameters());
			System.out.println("[DIAG] with @BeforeStep added        = " + CallLog.drain());
		};
	}

}
