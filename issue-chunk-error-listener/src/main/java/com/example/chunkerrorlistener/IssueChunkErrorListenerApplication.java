package com.example.chunkerrorlistener;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class IssueChunkErrorListenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueChunkErrorListenerApplication.class, args);
	}

	@Bean
	@Profile("!test")
	public CommandLineRunner diagnostics(JobOperator jobOperator, Job interfaceJob, Job annotationJob,
			Job legacyAnnotationJob) {
		return args -> {
			runAndReport(jobOperator, interfaceJob, "ChunkListener interface   ");
			runAndReport(jobOperator, annotationJob, "@OnChunkError             ");
			runAndReport(jobOperator, legacyAnnotationJob, "@AfterChunkError (5.x)    ");
		};
	}

	/** Both jobs fail on purpose. Only the recorded callbacks matter. */
	private void runAndReport(JobOperator jobOperator, Job job, String label) {
		try {
			jobOperator.start(job, new JobParameters());
		}
		catch (Exception expected) {
			// the writer throws on item 3
		}
		System.out.println("[DIAG] " + label + " = " + CallLog.drain());
	}

}
