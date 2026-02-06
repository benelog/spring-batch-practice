package kr.co.wikibook.batch.healthchecker.url;

import java.net.http.HttpConnectTimeoutException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import kr.co.wikibook.batch.healthchecker.listener.EmailJobReporter;
import kr.co.wikibook.batch.healthchecker.listener.LogResourceListener;
import kr.co.wikibook.batch.healthchecker.listener.RetryItemRecorder;
import kr.co.wikibook.batch.healthchecker.listener.RetryLogListener;
import kr.co.wikibook.batch.healthchecker.listener.SkipItemRecorder;
import kr.co.wikibook.batch.healthchecker.listener.StepLogListener;
import kr.co.wikibook.batch.healthchecker.util.Configs;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CheckUrlJobConfig {

  static final String INPUT_FILE_PARAM = "urlListFile";
  static final String OUTPUT_FILE_PATH = "./status.csv";
  private static final String INPUT_FILE_PARAM_EXP =
      "#{jobParameters['" + INPUT_FILE_PARAM + "']}"; // <1>

  private final JobRepository jobRepository;

  public CheckUrlJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job checkUrlJob(JavaMailSender mailSender) {
    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{INPUT_FILE_PARAM});

    var emailReporter = new EmailJobReporter(mailSender, List.of("benelog@naver.com"), true);
    return new JobBuilder("checkUrlJob", jobRepository)
        .listener(emailReporter)
        .listener(logResourceListener(null))
        .preventRestart()
        .validator(validator)
        .start(checkUrlStep())
        .build();
  }

  @Bean
  @JobScope
  public LogResourceListener logResourceListener(
      @Value(INPUT_FILE_PARAM_EXP) FileSystemResource urlListFile) {
    return new LogResourceListener(urlListFile);
  }

  @Bean
  public Step logResourceMetaStep() {
    return new StepBuilder("logResourceMetaStep", jobRepository)
        .allowStartIfComplete(true)
        .startLimit(5)
        .tasklet(logResourceMetaTaslket(null))
        .build();
  }

  @Bean
  public Step checkUrlStep() {
    var retryPolicy = RetryPolicy.builder()
        .maxRetries(3)
        .includes(HttpConnectTimeoutException.class)
        .delay(Duration.ofMillis(100L))
        .multiplier(2.0d)
        .maxDelay(Duration.ofMillis(600L))
        .build();

    var skipItemRecorder =
        new SkipItemRecorder<>(Path.of("skipped-url.txt"));

    var retryItemRecorder =
        new RetryItemRecorder(Path.of("retied.txt"));

    return new StepBuilder("checkUrlStep", jobRepository)
        .listener(new StepLogListener<>())
        .<String, ResponseStatus>chunk(10)
        .stream(skipItemRecorder)
        .stream(retryItemRecorder)
        .reader(urlFileReader(null))
        .processor(callUrlProcessor(null))
        .writer(buildResponseStatusFileWriter())
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(2)
        .retryPolicy(retryPolicy)
        .listener(skipItemRecorder)
        .listener(new RetryLogListener())
        .listener(retryItemRecorder)
        .build();
  }

  @Bean
  @JobScope
  public Tasklet logResourceMetaTaslket(@Value(INPUT_FILE_PARAM_EXP) FileSystemResource urlListFile) {
    var task = new LogResourceMetaTask(urlListFile);
    return new CallableTaskletAdapter(task);
  }

  @Bean
  @JobScope
  public FlatFileItemReader<String> urlFileReader(
      @Value(INPUT_FILE_PARAM_EXP) FileSystemResource urlListFile) {
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
    var outputFile = new FileSystemResource("status.csv");
    var writer = new FlatFileItemWriterBuilder<ResponseStatus>()
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
