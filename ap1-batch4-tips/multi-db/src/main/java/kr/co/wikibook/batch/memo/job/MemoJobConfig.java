package kr.co.wikibook.batch.memo.job;

import javax.sql.DataSource;
import kr.co.wikibook.batch.memo.etl.MemoComponents;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MemoJobConfig {

  private final JobRepository jobRepository;

  public MemoJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job memoJob() {
    return new JobBuilder("memoJob")
        .repository(this.jobRepository)
        .start(memoStep(null, null, null))
        .build();
  }

  @Bean
  @JobScope
  public Step memoStep(
      @Value("#{jobParameters['memoFile']}") Resource memoFile,
      DataSource mainDataSource,
      PlatformTransactionManager mainTransactionManager
  ) {
    ItemReader<String> reader = MemoComponents.memoFileReader(memoFile);
    ItemWriter<String> writer = MemoComponents.memoDbWriter(mainDataSource);
    return new StepBuilder("memoStep")
        .repository(this.jobRepository)
        .<String, String>chunk(10)
        .reader(reader)
        .writer(writer)
        .transactionManager(mainTransactionManager)
        .build();
  }
}
