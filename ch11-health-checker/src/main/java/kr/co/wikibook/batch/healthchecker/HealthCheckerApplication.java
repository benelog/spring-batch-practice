package kr.co.wikibook.batch.healthchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HealthCheckerApplication {

  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(HealthCheckerApplication.class,
        args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }
}
