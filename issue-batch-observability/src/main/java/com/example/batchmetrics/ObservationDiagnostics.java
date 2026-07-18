package com.example.batchmetrics;

import java.lang.reflect.Field;
import java.util.List;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

// Prints the evidence on shutdown, after the job has run.
@Component
public class ObservationDiagnostics implements ApplicationListener<ContextClosedEvent> {

  private final ApplicationContext context;

  public ObservationDiagnostics(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    try {
      MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
      List<String> batchMeters = meterRegistry.getMeters().stream()
          .map(meter -> meter.getId().getName())
          .filter(name -> name.startsWith("spring.batch"))
          .sorted().distinct().toList();
      System.out.println("[DIAG] spring.batch meters recorded = " + batchMeters);
      boolean launchCountPresent = batchMeters.stream()
          .anyMatch(name -> name.startsWith("spring.batch.job.launch"));
      System.out.println("[DIAG] spring.batch.job.launch.count present = " + launchCountPresent);
      meterRegistry.getMeters().stream()
          .filter(meter -> meter.getId().getName().equals("spring.batch.job"))
          .forEach(meter -> {
            if (meter instanceof io.micrometer.core.instrument.Timer timer) {
              System.out.println("[DIAG] spring.batch.job timer count = " + timer.count()
                  + " (the job ran once; a count of 2 means duplicated handlers)");
            }
          });

      Object operator = context.getBean("jobOperator");
      System.out.println("[DIAG] jobOperator bean class = " + operator.getClass().getName());
      if (operator instanceof Advised advised) {
        operator = advised.getTargetSource().getTarget();
      }
      System.out.println("[DIAG] jobOperator target class = " + operator.getClass().getName()
          + ", observationRegistry = " + readField(operator, "observationRegistry"));
    } catch (Exception ex) {
      System.out.println("[DIAG] error: " + ex);
    }
  }

  private Object readField(Object target, String fieldName) throws IllegalAccessException {
    for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
      try {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
      } catch (NoSuchFieldException ex) {
        // keep looking in the superclass
      }
    }
    return "field not found";
  }
}
