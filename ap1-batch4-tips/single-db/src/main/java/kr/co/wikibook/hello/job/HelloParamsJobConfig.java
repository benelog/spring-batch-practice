package kr.co.wikibook.hello.job;

import java.time.LocalDate;
import kr.co.wikibook.hello.tasklet.HelloDateTasklet;
import kr.co.wikibook.hello.tasklet.HelloLocalDateTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class HelloParamsJobConfig {
  PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();

  @Bean
  public Job helloParamsJob(JobRepository jobRepository) {
    Step helloDateStep = new StepBuilder("helloDateStep")
        .repository(jobRepository)
        .tasklet(new HelloDateTasklet())
        .transactionManager(transactionManager)
        .build();

    Step helloLocalDateStep = new StepBuilder("helloLocalDateStep")
        .repository(jobRepository)
        .tasklet(helloLocalDateTask(null))
        .transactionManager(transactionManager)
        .build();

    return new JobBuilder("helloParamsJob")
        .repository(jobRepository)
        .start(helloDateStep)
        .next(helloLocalDateStep)
        .build();
  }

  @Bean
  @JobScope
  public HelloLocalDateTasklet helloLocalDateTask(
      @Value("#{jobParameters['helloLocalDate']}")
      @DateTimeFormat(pattern = "yyyy.MM.dd")
      LocalDate helloLocalDate
  ) {
    return new HelloLocalDateTasklet(helloLocalDate);
  }
}
