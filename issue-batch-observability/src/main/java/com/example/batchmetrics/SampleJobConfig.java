package com.example.batchmetrics;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleJobConfig {

  // The step is registered as a bean on purpose:
  // BatchObservabilityBeanPostProcessor injects the ObservationRegistry into job/step beans,
  // so spring.batch.job and spring.batch.step ARE recorded. Only the JobOperator is left out.
  @Bean
  public Step sampleStep(JobRepository jobRepository) {
    return new StepBuilder("sampleStep", jobRepository)
        .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
        .build();
  }

  @Bean
  public Job sampleJob(JobRepository jobRepository, Step sampleStep) {
    return new JobBuilder("sampleJob", jobRepository)
        .start(sampleStep)
        .build();
  }
}
