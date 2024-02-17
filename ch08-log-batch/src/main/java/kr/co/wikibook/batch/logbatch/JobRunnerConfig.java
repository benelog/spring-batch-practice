package kr.co.wikibook.batch.logbatch;


import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

//@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class JobRunnerConfig {

  @Bean
  @ConditionalOnProperty(prefix = "spring.batch.job", name = "enabled", havingValue = "true", matchIfMissing = true)
  public JobLauncherApplicationRunner jobLauncherApplicationRunner(
      JobLauncher jobLauncher, JobExplorer jobExplorer, JobRepository jobRepository, BatchProperties properties) {
    var runner = new JobLauncherApplicationRunner(jobLauncher, jobExplorer, jobRepository);
    String jobName = properties.getJob().getName();
    if (StringUtils.hasText(jobName)) {
      runner.setJobName(jobName);
    }
    return runner;
  }
}
