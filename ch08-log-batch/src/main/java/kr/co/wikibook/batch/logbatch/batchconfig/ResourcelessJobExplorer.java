package kr.co.wikibook.batch.logbatch.batchconfig;

import java.util.List;
import java.util.Set;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;

public class ResourcelessJobExplorer implements JobExplorer {

  private final JobRepository jobRepository;

  public ResourcelessJobExplorer(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Override
  public List<JobExecution> getJobExecutions(JobInstance jobInstance) {
    return jobRepository.findJobExecutions(jobInstance);
  }

  @Override
  public List<String> getJobNames() {
    return jobRepository.getJobNames();
  }

  @Override
  public List<JobInstance> findJobInstancesByJobName(String jobName, int start, int count) {
    return jobRepository.findJobInstancesByName(jobName, start, count);
  }

  public List<JobInstance> getJobInstances(String jobName, int start, int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JobExecution getJobExecution(Long executionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StepExecution getStepExecution(Long jobExecutionId, Long stepExecutionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JobInstance getJobInstance(Long instanceId) {
    throw new UnsupportedOperationException();
  }


  @Override
  public Set<JobExecution> findRunningJobExecutions(String jobName) {
    throw new UnsupportedOperationException();
  }



  @Override
  public long getJobInstanceCount(String jobName) throws NoSuchJobException {
    throw new UnsupportedOperationException();
  }

  @Override
  public JobInstance getLastJobInstance(String jobName) {
    return null;
  }
}
