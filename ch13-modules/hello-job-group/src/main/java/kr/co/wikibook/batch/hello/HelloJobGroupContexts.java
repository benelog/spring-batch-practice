package kr.co.wikibook.batch.hello;

import kr.co.wikibook.batch.hello.job.Hello2JobConfig;
import kr.co.wikibook.batch.hello.job.SpendTimeChunkJobConfig;
import kr.co.wikibook.batch.hello.job.HelloJobConfig;
import kr.co.wikibook.batch.hello.job.HelloParamJobConfig;
import kr.co.wikibook.batch.hello.job.SlowJobConfig;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.context.annotation.Bean;

public class HelloJobGroupContexts {
  @Bean
  public ApplicationContextFactory helloJobContext() {
    return new GenericApplicationContextFactory(HelloJobConfig.class);
  }

  @Bean
  public ApplicationContextFactory hello2JobContext() {
    return new GenericApplicationContextFactory(Hello2JobConfig.class);
  }

  @Bean
  public ApplicationContextFactory helloParamsJobContext() {
    return new GenericApplicationContextFactory(HelloParamJobConfig.class);
  }

  @Bean
  public ApplicationContextFactory spendTimeChunkJobContext() {
    return new GenericApplicationContextFactory(SpendTimeChunkJobConfig.class);
  }

  @Bean
  public ApplicationContextFactory slowJobContext() {
    return new GenericApplicationContextFactory(SlowJobConfig.class);
  }
}
