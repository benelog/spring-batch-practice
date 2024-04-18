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
import org.springframework.mail.javamail.JavaMailSender;
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
  public LogResourceListener logResourceListener(@Value(INPUT_FILE_PARAM_EXP) PathResource urlListFile) {
    return new LogResourceListener(urlListFile);
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
    retryPolicy.setTimeout(1000L);
    var backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(200L);

    var skipItemRecorder =
        new SkipItemRecorder<>(Path.of("skipped-url.txt"));

    var retryItemRecorder =
        new RetryItemRecorder(Path.of("retied.txt"));

    return new StepBuilder("checkUrlStep", jobRepository)
        .listener(new StepLogListener<>())
        .<String, ResponseStatus>chunk(10, transactionManager)
        .stream(skipItemRecorder)
        .stream(retryItemRecorder)
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
        .listener(skipItemRecorder)
        .listener(new RetryLogListener())
        .listener(retryItemRecorder)
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
