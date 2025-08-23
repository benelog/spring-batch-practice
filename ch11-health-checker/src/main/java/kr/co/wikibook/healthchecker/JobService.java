package kr.co.wikibook.healthchecker;

import java.util.Set;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(description = "Job을 제어")
public class JobService {
  private final JobOperator operator;

  public JobService(JobOperator operator) {
    this.operator = operator;
  }

  @ManagedOperation(description = "Job 이름으로 JobExecution을 중지")
  public void stopExecutions(String jobName) throws JobExecutionException {
    for (long execId : operator.getRunningExecutions(jobName)) {
      operator.stop(execId);
    }
  }

  @ManagedOperation
  public void stopAllExecutions() throws JobExecutionException {
    Set<String> jobNames = operator.getJobNames();
    for (String jobName : jobNames) {
      Set<Long> runningExecutions = operator.getRunningExecutions(jobName);
      for (long execId : runningExecutions) {
        operator.stop(execId);
      }
    }
  }
}
