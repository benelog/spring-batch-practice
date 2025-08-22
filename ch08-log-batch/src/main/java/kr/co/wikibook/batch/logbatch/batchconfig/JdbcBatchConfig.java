package kr.co.wikibook.batch.logbatch.batchconfig;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;

@Configuration
public class JdbcBatchConfig extends DefaultBatchConfiguration {
  private final BatchProperties properties;
  private final PlatformTransactionManager transactionManager;

  private final DataSource dataSource;

  public JdbcBatchConfig(
      BatchProperties properties,
      @Qualifier("jobDbTransactionManager") PlatformTransactionManager transactionManager,
      @Qualifier("jobDataSource") DataSource dataSource) {
    this.properties = properties;
    this.transactionManager = transactionManager;
    this.dataSource = dataSource;
  }

  @Override
  protected DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  protected PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  @Override
  protected String getTablePrefix() {
    String tablePrefix = this.properties.getJdbc().getTablePrefix();
    return (tablePrefix != null) ? tablePrefix : super.getTablePrefix();
  }

  @Override
  protected Isolation getIsolationLevelForCreate() {
    Isolation isolation = this.properties.getJdbc().getIsolationLevelForCreate();
    return (isolation != null) ? isolation : super.getIsolationLevelForCreate();
  }

  @Override
  protected ExecutionContextSerializer getExecutionContextSerializer() {
    return new Jackson2ExecutionContextStringSerializer();
  }

  @Bean
  public DataSourceScriptDatabaseInitializer metaDbInit() {
    return new BatchDataSourceScriptDatabaseInitializer(dataSource, properties.getJdbc());
  }
}
