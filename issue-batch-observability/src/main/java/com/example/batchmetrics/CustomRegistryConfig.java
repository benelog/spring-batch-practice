package com.example.batchmetrics;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Activate with --spring.profiles.active=custom-registry to verify that declaring
// an ObservationRegistry bean (as suggested by the reference documentation)
// still does not connect it to the JobOperator on the Boot auto-configuration path.
@Configuration
@Profile("custom-registry")
public class CustomRegistryConfig {

  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }
}
