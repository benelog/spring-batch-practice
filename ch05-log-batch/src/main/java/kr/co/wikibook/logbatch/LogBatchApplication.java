package kr.co.wikibook.logbatch;

import kr.co.wikibook.logbatch.ColorConverters.ColorToStringConverter;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchConversionServiceCustomizer;
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
  public ExecutionContextSerializer executionContextSerializer() {
    return new Jackson2ExecutionContextStringSerializer();
  }

  @Bean
  public BatchConversionServiceCustomizer conversionServiceCustomizer() {
    return configurableConversionService -> {
      configurableConversionService.addConverter(new ColorToStringConverter());
    };
  }
}
