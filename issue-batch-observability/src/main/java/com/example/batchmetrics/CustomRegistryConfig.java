package com.example.batchmetrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Activate with --spring.profiles.active=custom-registry to verify that declaring
// an ObservationRegistry bean does not connect it to the JobOperator
// on the Boot auto-configuration path.
// This is the exact snippet from the reference documentation:
// https://docs.spring.io/spring-batch/reference/spring-batch-observability/micrometer.html
@Configuration
@Profile("custom-registry")
public class CustomRegistryConfig {

  @Bean
  public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig()
        .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
    return observationRegistry;
  }
}
