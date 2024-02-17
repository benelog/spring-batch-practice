package kr.co.wikibook.batch.logbatch;

import javax.sql.DataSource;
import org.springframework.batch.core.Job;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

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
        .next(buildDbToCsvStep())
        .build();
  }

  @Bean
  public TaskletStep csvToDbStep(
      @Qualifier("mainTransactionManager") PlatformTransactionManager transactionManager
  ) {
    ItemStreamReader<AccessLog> csvReader = this.accessLogCsvReader(null);
    JdbcBatchItemWriter<AccessLog> dbWriter = AccessLogComponents.buildAccessLogDbWriter(this.dataSource);
    return new StepBuilder("accessLogCsvToDb", this.jobRepository)
        .<AccessLog, AccessLog>chunk(300, transactionManager)
        .reader(csvReader)
        .processor(new AccessLogProcessor())
        .writer(dbWriter)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<AccessLog> accessLogCsvReader(
      @Value("#{jobParameters['accessLog']}") Resource resource) {

    return new FlatFileItemReaderBuilder<AccessLog>()
        .name("accessLogCsvReader")
        .resource(resource)
        .lineTokenizer(new DelimitedLineTokenizer())
        .fieldSetMapper(new AccessLogFieldSetMapper())
        .build();
  }


  private TaskletStep buildDbToCsvStep() {
    var userAccessOutput = new PathResource("user-access-summary.csv");
    JdbcCursorItemReader<UserAccessSummary> reader = UserAccessSummaryComponents.buildDbCursorReader(dataSource, false);
    FlatFileItemWriter<UserAccessSummary> writer = UserAccessSummaryComponents.buildCsvWriter(userAccessOutput);

    return new StepBuilder("userAccessSummaryDbToCsv", jobRepository)
        .<UserAccessSummary, UserAccessSummary>chunk(300, new ResourcelessTransactionManager())
        .reader(reader)
        .writer(writer)
        .build();
  }
}
