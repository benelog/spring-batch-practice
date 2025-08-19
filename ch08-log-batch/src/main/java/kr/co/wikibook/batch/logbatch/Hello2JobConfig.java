package kr.co.wikibook.batch.logbatch;

import kr.co.wikibook.batch.logbatch.bootconfig.DummyJobExplorer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = Hello2JobConfig.JOB_NAME)
public class Hello2JobConfig extends DefaultBatchConfiguration {

  public static final String JOB_NAME = "hello2Job";


  @Bean
  public Job hello2Job(JobRepository jobRepository) {
    var tasklet = new HelloTasklet();
    var transactionManager = new ResourcelessTransactionManager();

    Step helloStep = new StepBuilder("hello2Step", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();

    return new JobBuilder(JOB_NAME, jobRepository)
        .start(helloStep)
        .build();
  }

  @Override
  public JobRepository jobRepository() throws BatchConfigurationException {
    return new ResourcelessJobRepository();
  }

  @Override
  public JobExplorer jobExplorer() throws BatchConfigurationException {
    return new DummyJobExplorer();
  }

  @Override
  protected PlatformTransactionManager getTransactionManager() {
    return new ResourcelessTransactionManager();
  }
}
