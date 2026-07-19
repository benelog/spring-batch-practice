package com.example.plainbatch;

import javax.sql.DataSource;

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

// Batch infrastructure WITHOUT any ObservationRegistry bean.
// The registry comes from RegistryConfig, registered before or after this class
// depending on the scenario.
@Configuration
@EnableBatchProcessing
public class BatchInfraConfig {

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
