package kr.co.wikibook.batch.logbatch.batchconfig;


import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.batch.autoconfigure.BatchProperties;
import org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
@EnableConfigurationProperties(BatchProperties.class)
public class JobRunnerConfig {

  @Bean
  @ConditionalOnProperty(prefix = "spring.batch.job", name = "enabled", havingValue = "true", matchIfMissing = true)
  public JobLauncherApplicationRunner jobLauncherApplicationRunner(
      JobOperator operator,
      BatchProperties properties
  ) {
    var runner = new JobLauncherApplicationRunner(operator);
    String jobName = properties.getJob().getName();

    if (StringUtils.hasText(jobName)) {
      runner.setJobName(jobName);
    }
    runner.setOrder(Ordered.LOWEST_PRECEDENCE);
    return runner;
  }
}
