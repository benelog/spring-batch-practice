package com.example.plainbatch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A configuration class WITHOUT @EnableBatchProcessing.
// The observationRegistry bean is the exact snippet from the reference documentation:
// https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html
// (correct in a non-Boot application: the handler must be registered manually).
@Configuration
public class RegistryConfig {

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  @Bean
  public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig()
        .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
    return observationRegistry;
  }
}
