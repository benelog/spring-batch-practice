package com.example.duplicatelistener;

import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
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
	public Job annotatedJob() {
		return new JobBuilder("annotatedJob", this.jobRepository).start(step("annotatedStep", new AnnotatedProcessor()))
			.build();
	}

	@Bean
	public Job plainJob() {
		return new JobBuilder("plainJob", this.jobRepository).start(step("plainStep", new PlainProcessor())).build();
	}

	private Step step(String name, ItemProcessor<Integer, Integer> processor) {
		return new StepBuilder(name, this.jobRepository).<Integer, Integer>chunk(2)
			.transactionManager(new ResourcelessTransactionManager())
			.reader(new ListItemReader<>(List.of(1, 2)))
			.processor(processor)
			.writer(chunk -> {
			})
			.build();
	}

}
