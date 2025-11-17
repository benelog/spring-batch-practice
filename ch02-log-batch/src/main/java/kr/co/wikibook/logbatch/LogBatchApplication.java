package kr.co.wikibook.logbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogBatchApplication {
  public static void main(String[] args) {
    SpringApplication.run(LogBatchApplication.class, args);
  }

  @Bean
  public NotificationService notificationService() {
    return new LoggingService();
  }
}
