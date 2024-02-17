package kr.co.wikibook.batch.logbatch;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.core.DataClassRowMapper;

public class UserAccessSummaryComponents {
  public static FlatFileItemWriter<UserAccessSummary> buildCsvWriter(WritableResource resource) {
    var writer =  new FlatFileItemWriterBuilder<UserAccessSummary>()
        .name("userAccessSummaryCsvWriter")
        .resource(resource)
        .formatted()
        .format("%s,%d")
        .fieldExtractor(new UserAccessSummaryFieldSetExtractor())
        .build();
    return Configs.afterPropertiesSet(writer);
  }

  public static JdbcCursorItemReader<UserAccessSummary> buildDbCursorReader(DataSource dataSource, boolean sharedConection) {
    var reader =  new JdbcCursorItemReaderBuilder<UserAccessSummary>()
        .name("userAccessSummaryDbReader")
        .dataSource(dataSource)
        .sql(AccessLogSql.COUNT_GROUP_BY_USERNAME)
        .useSharedExtendedConnection(sharedConection)
        .rowMapper(new DataClassRowMapper<>(UserAccessSummary.class))
        .build();
    return Configs.afterPropertiesSet(reader);
  }

  public static JdbcPagingItemReader<UserAccessSummary> buildDbPagingReader(DataSource dataSource, int pageSize) {
    PagingQueryProvider queryProvider = buildPagingQueryProvider(dataSource);
    var reader = new JdbcPagingItemReaderBuilder<UserAccessSummary>()
        .name("accessLogDbReader")
        .dataSource(dataSource)
        .queryProvider(queryProvider)
        .rowMapper(new DataClassRowMapper<>(UserAccessSummary.class))
        .pageSize(pageSize)
        .build();
    return Configs.afterPropertiesSet(reader);
  }

  private static PagingQueryProvider buildPagingQueryProvider(DataSource dataSource) {
    var factory = new SqlPagingQueryProviderFactoryBean();
    factory.setDataSource(dataSource);
    factory.setSelectClause("username, COUNT(1) AS access_count");
    factory.setFromClause("access_log");
    factory.setGroupClause("username");
    factory.setSortKeys(Map.of("username", Order.ASCENDING));
    try {
      return factory.getObject();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}

