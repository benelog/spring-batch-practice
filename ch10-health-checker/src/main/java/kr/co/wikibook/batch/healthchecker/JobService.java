package kr.co.wikibook.batch.healthchecker;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobService implements CommandLineRunner {
  private final JobOperator operator;

  public JobService(JobOperator operator) {
    this.operator = operator;
  }

  @Override
  public void run(String... args) throws Exception {
    for (long execId : operator.getRunningExecutions("slowJob")) {
      operator.stop(execId);
    }
  }
}
