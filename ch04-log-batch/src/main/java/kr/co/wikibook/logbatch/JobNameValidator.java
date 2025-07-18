package kr.co.wikibook.logbatch;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JobNameValidator implements ApplicationRunner {
  private final JobLocator locator;
  private final String jobName;

  public JobNameValidator(BatchProperties properties, JobLocator locator) {
    this.jobName = properties.getJob().getName();
    this.locator = locator;
  }

  @Override
  public void run(ApplicationArguments args) throws NoSuchJobException {
    if (this.jobName == null || this.jobName.isBlank()) {
      return;
    }
    this.locator.getJob(this.jobName);
  }
}
