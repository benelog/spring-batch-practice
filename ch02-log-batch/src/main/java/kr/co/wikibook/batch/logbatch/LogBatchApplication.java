package kr.co.wikibook.batch.logbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableTask
public class LogBatchApplication {
  public static void main(String[] args) {
    SpringApplication.run(LogBatchApplication.class, args);
  }

  @Bean
  public NotificationService notificationService() {
    return new LoggingService();
  }
}
