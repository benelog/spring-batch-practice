package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Configuration
@ConditionalOnProperty("access-log")
public class AccessLogJobConfig {

  @Bean
  @Order(1)
  public CommandLineRunner accessLogCsvToDbTask(
      @Value("${access-log}") Resource resource,
      DataSource dataSource
  ) {
    var reader = new AccessLogCsvReader(resource);
    var writer = new AccessLogDbWriter(dataSource);
    return new AccessLogCsvToDbTask(reader, writer, 300);
  }

  @Bean
  @Order(2)
  public CommandLineRunner userAccessSummaryDbToCsvTask(DataSource dataSource) {
    var reader = new UserAccessSummaryDbReader(dataSource);
    var writer = new UserAccessSummaryCsvWriter(new FileSystemResource("user-access-summary.csv"));
    return new UserAccessSummaryDbToCsvTask(reader, writer, 300);
  }
}
