package com.example.duplicatelistener;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DuplicateListenerRegistrationTests {

	@Autowired
	private JobOperator jobOperator;

	@Test
	void callbacksFireOnceWhenOnlyTheInterfaceIsImplemented(@Autowired Job plainJob) throws Exception {
		this.jobOperator.start(plainJob, parameters("plain"));

		assertThat(CallLog.drain()).containsExactly("beforeProcess:1", "afterProcess:1", "beforeProcess:2",
				"afterProcess:2");
	}

	@Test
	void callbacksShouldAlsoFireOnceWhenAListenerAnnotationIsAdded(@Autowired Job annotatedJob) throws Exception {
		this.jobOperator.start(annotatedJob, parameters("annotated"));

		// Fails on 6.0.4: every ItemProcessListener callback is recorded twice.
		assertThat(CallLog.drain()).containsExactly("beforeStep", "beforeProcess:1", "afterProcess:1",
				"beforeProcess:2", "afterProcess:2");
	}

	private JobParameters parameters(String run) {
		return new JobParametersBuilder().addString("run", run).toJobParameters();
	}

}
