package kr.co.wikibook.hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.co.wikibook.hello.tasklet.HelloTasklet;

@Configuration
public class HelloJob2Config {
  @Bean
  public Job helloJob2(JobRepository jobRepository) {
    var transactionManager = new ResourcelessTransactionManager();
    Step helloStep = new StepBuilder("helloStep")
        .repository(jobRepository)
        .tasklet(new HelloTasklet())
        .transactionManager(transactionManager)
        .build();

    return new JobBuilder("helloJob2")
        .repository(jobRepository)
        .start(helloStep)
        .build();
  }
}
