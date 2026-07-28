package com.example.chunkerrorlistener;

import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleJobConfig {

	private final JobRepository jobRepository;

	public SampleJobConfig(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	@Bean
	public Job interfaceJob() {
		return new JobBuilder("interfaceJob", this.jobRepository)
			.start(step("interfaceStep", builder -> builder.listener(new InterfaceChunkListener())))
			.build();
	}

	@Bean
	public Job annotationJob() {
		return new JobBuilder("annotationJob", this.jobRepository)
			.start(step("annotationStep", builder -> builder.listener((Object) new AnnotatedChunkListener())))
			.build();
	}

	@Bean
	public Job legacyAnnotationJob() {
		return new JobBuilder("legacyAnnotationJob", this.jobRepository)
			.start(step("legacyAnnotationStep", builder -> builder.listener((Object) new LegacyAnnotatedChunkListener())))
			.build();
	}

	/** The writer fails on the second chunk, so a chunk error is guaranteed. */
	private Step step(String name, UnaryOperator<ChunkOrientedStepBuilder<Integer, Integer>> withListener) {
		ChunkOrientedStepBuilder<Integer, Integer> builder = new StepBuilder(name, this.jobRepository)
			.<Integer, Integer>chunk(2)
			.transactionManager(new ResourcelessTransactionManager())
			.reader(new NumberReader(List.of(1, 2, 3, 4)))
			.writer(chunk -> {
				if (chunk.getItems().contains(3)) {
					throw new IllegalStateException("write failed on item 3");
				}
			});
		return withListener.apply(builder).build();
	}

}
