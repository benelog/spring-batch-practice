package kr.co.wikibook.logbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AccessLogJobConfig {

  public static final String JOB_NAME = "accessLogJob";

  private final DataSource dataSource;
  private final JobRepository jobRepository;

  public AccessLogJobConfig(DataSource dataSource, JobRepository jobRepository) {
    this.dataSource = dataSource;
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job accessLogJob() {
    return new JobBuilder(JOB_NAME, this.jobRepository)
            .start(this.csvToDbStep(null))
            .build();
  }

  @Bean
  public TaskletStep csvToDbStep(JdbcTransactionManager transactionManager) {
    ItemStreamReader<AccessLog> csvReader = this.accessLogCsvReader(null); // <1>
    return new StepBuilder("accessLogCsvToDb", this.jobRepository)
            .<AccessLog, AccessLog>chunk(300, transactionManager)
            .reader(csvReader)
            .processor(new AccessLogProcessor())
            .writer(new AccessLogDbWriter(this.dataSource))
            .build();
  }

  @Bean
  @StepScope // <2>
  public AccessLogCsvReader accessLogCsvReader(
          @Value("#{jobParameters['accessLog']}") Resource resource) { // <3>
    return new AccessLogCsvReader(resource);
  }
}
