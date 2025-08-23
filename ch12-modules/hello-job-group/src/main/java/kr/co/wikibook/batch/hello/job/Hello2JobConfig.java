package kr.co.wikibook.batch.hello.job;

import kr.co.wikibook.batch.hello.tasklet.HelloTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;

public class Hello2JobConfig {

  public static final String JOB_NAME = "hello2Job";

  @Bean
  public Job hello2Job(JobRepository jobRepository) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(helloStep(jobRepository))
        .build();
  }

  @Bean
  public Step helloStep(JobRepository jobRepository) {
    var transactionManager = new ResourcelessTransactionManager();
    var tasklet = new HelloTasklet();
    return new StepBuilder("hello2Step", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();
  }
}
