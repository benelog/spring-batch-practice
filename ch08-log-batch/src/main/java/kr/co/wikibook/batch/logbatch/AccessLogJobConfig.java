package kr.co.wikibook.batch.logbatch;

import java.nio.file.Path;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class AccessLogJobConfig {

  public static final String JOB_NAME = "accessLogJob";

  private final DataSource dataSource;
  private final JobRepository jobRepository;
  private final Path basePath;

  public AccessLogJobConfig(
      DataSource dataSource, JobRepository jobRepository,
      @Value("${base-path:./logs}") Path basePath) {
    this.dataSource = dataSource;
    this.jobRepository = jobRepository;
    this.basePath = basePath;
  }

  @Bean
  public Job accessLogJob() {
    return new JobBuilder(JOB_NAME, this.jobRepository)
        .start(this.csvToDbStep(null))
        .next(this.dbToCsvStep(null))
        .build();
  }

  @Bean
  public TaskletStep csvToDbStep(
      @Qualifier("mainTransactionManager") JdbcTransactionManager transactionManager
  ) {
    ItemStreamReader<AccessLog> reader = this.accessLogCsvReader(null);
    JdbcBatchItemWriter<AccessLog> writer = AccessLogComponents.buildAccessLogDbWriter(this.dataSource);
    return new StepBuilder("accessLogCsvToDb", this.jobRepository)
        .<AccessLog, AccessLog>chunk(300, transactionManager)
        .reader(reader)
        .processor(new AccessLogProcessor())
        .writer(writer)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<AccessLog> accessLogCsvReader(
      @Value("#{jobParameters['date']}") LocalDate date
  ) {
    var resource = new PathResource(basePath.resolve(date + ".csv"));

    return new FlatFileItemReaderBuilder<AccessLog>()
        .name("accessLogCsvReader")
        .resource(resource)
        .lineTokenizer(new DelimitedLineTokenizer())
        .fieldSetMapper(new AccessLogFieldSetMapper())
        .build();
  }

  @Bean
  @JobScope
  public TaskletStep dbToCsvStep(
      @Value("#{jobParameters['date']}") LocalDate date
  ) {
    var resource = new PathResource(basePath.resolve(date + "_summary.csv"));
    JdbcCursorItemReader<UserAccessSummary> reader = UserAccessSummaryComponents.buildDbCursorReader(dataSource, date, false);
    FlatFileItemWriter<UserAccessSummary> writer = UserAccessSummaryComponents.buildCsvWriter(resource);

    return new StepBuilder("userAccessSummaryDbToCsv", jobRepository)
        .<UserAccessSummary, UserAccessSummary>chunk(300, new ResourcelessTransactionManager())
        .reader(reader)
        .writer(writer)
        .build();
  }
}
