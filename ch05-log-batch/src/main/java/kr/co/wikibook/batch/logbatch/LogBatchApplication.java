package kr.co.wikibook.batch.logbatch;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogBatchApplication {
  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(LogBatchApplication.class, args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }

  @Bean
  public ExitCodeGenerator codeGenerator() {
    return () -> 3;
  }

  @Bean
  public ExecutionContextSerializer executionContextSerializer() {
    return new Jackson2ExecutionContextStringSerializer();
  }
}
