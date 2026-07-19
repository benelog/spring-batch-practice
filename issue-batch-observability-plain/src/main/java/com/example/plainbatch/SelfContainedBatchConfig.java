package com.example.plainbatch;

import javax.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

// Control scenario: the ObservationRegistry bean is declared INSIDE
// the @EnableBatchProcessing configuration class, so its bean definition
// always exists before BatchRegistrar runs.
@Configuration
@EnableBatchProcessing
public class SelfContainedBatchConfig {

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  @Bean
  public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig()
        .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
    return observationRegistry;
  }

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .generateUniqueName(true)
        .addScript("classpath:/org/springframework/batch/core/schema-h2.sql")
        .build();
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JdbcTransactionManager(dataSource());
  }

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
