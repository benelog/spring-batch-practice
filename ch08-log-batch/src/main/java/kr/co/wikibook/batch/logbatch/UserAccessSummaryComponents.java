package kr.co.wikibook.batch.logbatch;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.WritableResource;

public class UserAccessSummaryComponents {

  public static FlatFileItemWriter<UserAccessSummary> buildCsvWriter(WritableResource resource) {
    var writer = new FlatFileItemWriterBuilder<UserAccessSummary>()
        .name("userAccessSummaryCsvWriter")
        .resource(resource)
        .formatted()
        .format("%s,%d")
        .fieldExtractor(new UserAccessSummaryFieldSetExtractor())
        .build();
    return Configs.afterPropertiesSet(writer);
  }

  public static JdbcCursorItemReader<UserAccessSummary> buildDbCursorReader(
      DataSource dataSource, LocalDate date, boolean sharedConnection
  ) {
    Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant to = from.plus(1, ChronoUnit.DAYS);

    return new JdbcCursorItemReaderBuilder<UserAccessSummary>()
        .name("userAccessSummaryDbReader")
        .dataSource(dataSource)
        .useSharedExtendedConnection(sharedConnection)
        .sql(AccessLogSql.COUNT_GROUP_BY_USERNAME)
        .queryArguments(from, to)
        .dataRowMapper(UserAccessSummary.class)
        .build();
  }

  public static JdbcPagingItemReader<UserAccessSummary> buildDbPagingReader(
      DataSource dataSource, LocalDate date, int pageSize
  ) {
    Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant to = from.plus(1, ChronoUnit.DAYS);
    Map<String, Object> queryParams = Map.of("from", from, "to", to);

    try {
      return new JdbcPagingItemReaderBuilder<UserAccessSummary>()
          .name("accessLogDbReader")
          .dataSource(dataSource)
          .selectClause("username, COUNT(1) AS access_count")
          .fromClause("access_log")
          .whereClause("access_date_time BETWEEN :from AND :to")
          .groupClause("username")
          .sortKeys(Map.of("username", Order.ASCENDING))
          .parameterValues(queryParams)
          .pageSize(pageSize)
          .dataRowMapper(UserAccessSummary.class) // <1>
          .build();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
