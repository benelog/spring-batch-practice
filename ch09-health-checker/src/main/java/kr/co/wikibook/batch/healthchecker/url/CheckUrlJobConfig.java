package kr.co.wikibook.batch.healthchecker.url;

import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import kr.co.wikibook.batch.healthchecker.Configs;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CheckUrlJobConfig {
  static final String INPUT_FILE_PARAM = "urlListFile";
  static final String OUTPUT_FILE_PATH = "./status.csv";
  private static final String INPUT_FILE_PARAM_EXP = "#{jobParameters['" + INPUT_FILE_PARAM + "']}"; // <1>

  private final PlatformTransactionManager transactionManager = new ResourcelessTransactionManager(); // <2>

  private final JobRepository jobRepository;

  public CheckUrlJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job checkUrlJob() {
    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{INPUT_FILE_PARAM});
    return new JobBuilder("checkUrlJob", jobRepository)
        .preventRestart()
        .validator(validator)
        .start(logResourceMetaStep())
        .next(checkUrlStep())
        .build();
  }

  @Bean
  public Step logResourceMetaStep() {
    return new StepBuilder("logResourceMetaStep", jobRepository)
        .allowStartIfComplete(true)
        .startLimit(5)
        .tasklet(logResourceMetaTaslket(null), transactionManager)
        .build();
  }

  @Bean
  public Step checkUrlStep() {
    var retryPolicy = new TimeoutRetryPolicy();
    retryPolicy.setTimeout(500L);
    var backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(200L);

    return new StepBuilder("checkUrlStep", jobRepository)
        .<String, ResponseStatus>chunk(10, transactionManager)
        .reader(urlFileReader(null))
        .processor(callUrlProcessor(null))
        .writer(buildResponseStatusFileWriter())
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(2)
        .retry(HttpConnectTimeoutException.class)
        .retryLimit(3)
        .retryPolicy(retryPolicy)
        .noRollback(IllegalArgumentException.class)
        .backOffPolicy(backOffPolicy)
        .build();
  }

  @Bean
  @JobScope
  public Tasklet logResourceMetaTaslket(@Value(INPUT_FILE_PARAM_EXP) PathResource urlListFile) {
    var tasklet = new CallableTaskletAdapter();
    tasklet.setCallable(new LogResourceMetaTask(urlListFile));
    return tasklet;
  }

  @Bean
  @JobScope
  public FlatFileItemReader<String> urlFileReader(
      @Value(INPUT_FILE_PARAM_EXP) PathResource urlListFile) {
    return new FlatFileItemReaderBuilder<String>()
        .name("urlFileReader")
        .resource(urlListFile)
        .lineMapper(new PassThroughLineMapper()) // <4>
        .build();
  }

  @Bean
  public CallUrlProcessor callUrlProcessor(@Value("${request.timeout}") Duration requestTimeout) {
    return new CallUrlProcessor(requestTimeout);
  }
  private FlatFileItemWriter<ResponseStatus> buildResponseStatusFileWriter() { // <6>
    var outputFile = new PathResource("status.csv");
    var writer =  new FlatFileItemWriterBuilder<ResponseStatus>()
        .name("responseStatusFileWriter")
        .resource(outputFile)
        .delimited()
        .fieldExtractor(item -> new Object[]{
            item.statusCode(), item.responseTimeMillis() + "ms", item.url() // <7>
        })
        .build();
    return Configs.afterPropertiesSet(writer); // <8>
  }
}
