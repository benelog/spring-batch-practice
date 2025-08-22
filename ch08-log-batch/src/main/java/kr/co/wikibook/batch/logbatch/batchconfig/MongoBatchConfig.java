package kr.co.wikibook.batch.logbatch.batchconfig;

import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MongoJobExplorerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MongoJobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class MongoBatchConfig extends DefaultBatchConfiguration {
  private final MongoOperations mongoOperations;
  private final MongoTransactionManager transactionManager;

  public MongoBatchConfig(MongoOperations mongoOperations, MongoTransactionManager transactionManager) {
    this.mongoOperations = mongoOperations;
    this.transactionManager = transactionManager;
  }

  @Bean
  public JobRepository jobRepository() throws BatchConfigurationException {
    var factory = new MongoJobRepositoryFactoryBean();
    factory.setMongoOperations(this.mongoOperations);
    factory.setTransactionManager(this.transactionManager);
    try {
      factory.afterPropertiesSet();
      return factory.getObject();
    }	catch (Exception e) {
        throw new BatchConfigurationException("Unable to configure the MongoDB job repository", e);
      }
  }

  @Bean
  public JobExplorer jobExplorer() throws BatchConfigurationException {
    var factory = new MongoJobExplorerFactoryBean();
    factory.setMongoOperations(mongoOperations);
    try {
      factory.afterPropertiesSet();
      return factory.getObject();
    }
    catch (Exception e) {
      throw new BatchConfigurationException("Unable to configure the MongoDB job explorer", e);
    }
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }
}
