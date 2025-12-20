package kr.co.wikibook.healthchecker;

import java.util.Collection;
import java.util.Set;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(description = "Job을 제어")
public class JobService {
  private final JobOperator operator;
  private final JobRepository repository;

  public JobService(JobOperator operator, JobRepository repository) {
    this.operator = operator;
    this.repository = repository;
  }

  @ManagedOperation(description = "Job 이름으로 JobExecution을 중지")
  public void stopExecutions(String jobName) throws JobExecutionException {
    Set<JobExecution> runningExecutions = repository.findRunningJobExecutions(jobName);
    for (JobExecution execution : runningExecutions) {
      operator.stop(execution);
    }
  }
}
