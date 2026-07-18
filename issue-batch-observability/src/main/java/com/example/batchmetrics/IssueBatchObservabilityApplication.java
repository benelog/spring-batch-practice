package com.example.batchmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class IssueBatchObservabilityApplication {

  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(IssueBatchObservabilityApplication.class, args);
    System.exit(SpringApplication.exit(context));
  }
}
