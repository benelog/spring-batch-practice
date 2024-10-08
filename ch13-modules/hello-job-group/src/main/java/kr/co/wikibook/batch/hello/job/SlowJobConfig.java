package kr.co.wikibook.batch.hello.job;

import kr.co.wikibook.batch.hello.tasklet.RepeatSleepTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;

public class SlowJobConfig {
  @Bean
  public Job slowJob(JobRepository jobRepository) {
    var repeatSleepStep = new StepBuilder("repeatSleepStep", jobRepository)
        .tasklet(new RepeatSleepTasklet(), new ResourcelessTransactionManager())
        .build();

    return new JobBuilder("slowJob", jobRepository)
        .start(repeatSleepStep)
        .build();
  }
}
