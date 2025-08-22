package kr.co.wikibook.batch.logbatch.batchconfig;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class ResourcelessBatchConfig extends DefaultBatchConfiguration {
  @Bean
  public JobRepository jobRepository() throws BatchConfigurationException {
    return new ResourcelessJobRepository();
  }

  @Bean
  public JobExplorer jobExplorer() throws BatchConfigurationException {
    return new ResourcelessJobExplorer(jobRepository());
  }

  @Override
  protected PlatformTransactionManager getTransactionManager() {
    return new ResourcelessTransactionManager();
  }

  @Override
  protected DataSource getDataSource() {
    return new SingleConnectionDataSource();
  }
}
