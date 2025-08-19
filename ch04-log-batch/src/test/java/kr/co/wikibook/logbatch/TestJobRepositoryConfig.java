package kr.co.wikibook.logbatch;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class TestJobRepositoryConfig {
  @Bean
  @Primary
  public JobRepository testJobRepository() {
    return new ResourcelessJobRepository();
  }
}
