package kr.co.wikibook.batch;

import java.time.format.DateTimeFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

@SpringBootApplication
public class BatchApplication {
  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(BatchApplication.class, args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }

  @Bean
  public FormattingConversionService conversionService() {
    var conversionService = new DefaultFormattingConversionService(true);
    var registrar = new DateTimeFormatterRegistrar();
    registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    registrar.registerFormatters(conversionService);
    return conversionService;
  }
}
