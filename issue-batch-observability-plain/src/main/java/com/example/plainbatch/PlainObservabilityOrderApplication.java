package com.example.plainbatch;

import java.util.Set;
import java.util.TreeSet;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// Runs the same job in three plain-Spring (non-Boot) contexts to show that
// spring.batch.job.launch.count depends on the configuration-class registration order.
public class PlainObservabilityOrderApplication {

  public static void main(String[] args) throws Exception {
    run("A: RegistryConfig registered BEFORE the @EnableBatchProcessing class",
        RegistryConfig.class, BatchInfraConfig.class);
    run("B: RegistryConfig registered AFTER the @EnableBatchProcessing class",
        BatchInfraConfig.class, RegistryConfig.class);
    run("C: ObservationRegistry declared INSIDE the @EnableBatchProcessing class",
        SelfContainedBatchConfig.class);
  }

  private static void run(String scenario, Class<?>... configs) throws Exception {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configs)) {
      JobOperator jobOperator = context.getBean(JobOperator.class);
      Job job = context.getBean(Job.class);
      jobOperator.start(job, new JobParametersBuilder()
          .addLong("id", System.nanoTime())
          .toJobParameters());

      MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
      Set<String> names = new TreeSet<>();
      meterRegistry.forEachMeter(meter -> {
        String name = meter.getId().getName();
        if (name.startsWith("spring.batch")) {
          names.add(name);
        }
      });
      System.out.println("[SCENARIO " + scenario + "]");
      System.out.println("  spring.batch meters = " + names);
      System.out.println("  spring.batch.job.launch.count present = "
          + names.contains("spring.batch.job.launch.count"));
    }
  }
}
