package kr.co.wikibook.batch.support;

import java.util.Properties;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;

public class JobService {
  private final JobRegistry registry;
  private final JobOperator operator;
  private JobParametersConverter converter = new DefaultJobParametersConverter();

  public JobService(JobRegistry registry, JobOperator operator) {
    this.registry = registry;
    this.operator = operator;
  }

  public long start(String jobName, Properties parameters) {
    try {
      Job job = registry.getJob(jobName);
      JobParameters jobParameters = converter.getJobParameters(parameters);
      JobExecution execution = operator.start(job, jobParameters);
      return execution.getId();
    } catch (JobExecutionException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
