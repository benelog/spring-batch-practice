package kr.co.wikibook.batch.hello.job;

import java.time.LocalDate;
import kr.co.wikibook.batch.hello.tasklet.HelloDate1Tasklet;
import kr.co.wikibook.batch.hello.tasklet.HelloDate2Tasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class HelloParamJobConfig {
  @Bean
  public Job helloParamJob(JobRepository jobRepository) {
    var transactionManager = new ResourcelessTransactionManager();

    Step helloDate1Step = new StepBuilder("helloDate1Step", jobRepository)
        .tasklet(new HelloDate1Tasklet(), transactionManager)
        .build();

    Step helloDate2Step = new StepBuilder("helloDate2Step", jobRepository)
        .tasklet(helloDate2Tasklet(null), transactionManager)
        .build();

    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{"helloDate"});

    var incrementer = new RunIdIncrementer();
    incrementer.setKey("runId");

    return new JobBuilder("helloParamJob", jobRepository)
        .validator(validator)
        .incrementer(incrementer)
        .start(helloDate1Step)
        .next(helloDate2Step)
        .build();
  }

  @Bean
  @JobScope
  public HelloDate2Tasklet helloDate2Tasklet(
      @Value("#{jobParameters['helloDate']}") LocalDate date) {
    return new HelloDate2Tasklet(date);
  }
}
