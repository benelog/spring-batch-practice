package com.example.chunkerrorlistener;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ChunkErrorListenerTests {

	private static final AtomicLong RUN = new AtomicLong();

	@Autowired
	private JobOperator jobOperator;

	@Autowired
	private Job interfaceJob;

	@Autowired
	private Job annotationJob;

	@Autowired
	private Job legacyAnnotationJob;

	@BeforeEach
	void clearLog() {
		CallLog.drain();
	}

	@Test
	@DisplayName("ChunkListener 인터페이스를 구현하면 onChunkError가 호출된다")
	void interfaceCallbackIsInvoked() {
		assertThat(runAndDrain(this.interfaceJob)).contains("interface:onChunkError");
	}

	@Test
	@DisplayName("@BeforeChunk와 @AfterChunk는 애너테이션으로도 호출된다")
	void otherChunkAnnotationsAreInvoked() {
		assertThat(runAndDrain(this.annotationJob)).contains("annotation:beforeChunk", "annotation:afterChunk");
	}

	@Test
	@DisplayName("@OnChunkError는 호출되지 않는다 (버그가 고쳐지면 이 테스트가 통과한다)")
	void onChunkErrorAnnotationShouldBeInvoked() {
		assertThat(runAndDrain(this.annotationJob)).contains("annotation:onChunkError");
	}

	@Test
	@DisplayName("@AfterChunkError도 호출되지 않는다 (청크 기반 스텝에서는 등록조차 되지 않는다)")
	void afterChunkErrorAnnotationShouldBeInvoked() {
		assertThat(runAndDrain(this.legacyAnnotationJob)).contains("legacy:afterChunkError");
	}

	/** Every job fails on purpose: the writer throws on item 3. */
	private List<String> runAndDrain(Job job) {
		try {
			this.jobOperator.start(job, new JobParametersBuilder().addLong("run", RUN.incrementAndGet())
				.toJobParameters());
		}
		catch (Exception expected) {
			// the chunk error is what we are measuring
		}
		return CallLog.drain();
	}

}
