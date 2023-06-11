package kr.co.wikibook.batch;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobStepBuilderConfig {

  @Bean
  public JobBuilderFactory jobBuilders(JobRepository jobRepository) {
    return new JobBuilderFactory(jobRepository);
  }

  @Bean
  public StepBuilderFactory stepBuilders(JobRepository jobRepository, PlatformTransactionManager mainTransactionManager) {
    return new StepBuilderFactory(jobRepository, mainTransactionManager);
  }
}
