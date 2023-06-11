package kr.co.wikibook.batch;

import javax.sql.DataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile("dev")
public class MainDbDevInit {
  @Bean
  public InitializingBean mainDbInitializer(
      @Qualifier("mainDataSource")
      DataSource mainDataSource
  ) {
    return () -> {
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
      populator.setContinueOnError(true);
      populator.addScript(new ClassPathResource("schema.sql"));
      DatabasePopulatorUtils.execute(populator, mainDataSource);
    };
  }
}
