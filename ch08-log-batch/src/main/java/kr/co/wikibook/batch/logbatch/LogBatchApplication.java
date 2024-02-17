package kr.co.wikibook.batch.logbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing(
    dataSourceRef = "jobDataSource",
    transactionManagerRef = "jobDbTransactionManager",
    isolationLevelForCreate = "ISOLATION_REPEATABLE_READ",
    executionContextSerializerRef = "jacksonSerializer"
)
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
  public ExecutionContextSerializer jacksonSerializer() {
    return new Jackson2ExecutionContextStringSerializer();
  }
}
